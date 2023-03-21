package it.olly.teamsconnectorsample.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.MailFolder;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.MailFolderCollectionPage;
import com.microsoft.graph.requests.MessageCollectionPage;

import it.olly.teamsconnectorsample.service.ms.MSClientHelper;

@RestController
@RequestMapping("/")
public class PageHTMLController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // CONFIG STUFF
    @Value("${ms.client_id}")
    public String CLIENT_ID;
    @Value("${ms.client_secret}")
    public String CLIENT_SECRET;
    @Value("${ms.redirect_uri}")
    public String REDIRECT_URI;
    @Value("${ms.scope}")
    public String SCOPE;
    @Value("${ms.webhooks.enabled:true}")
    public Boolean webhooksEnabled;
    @Value("${ms.chat.attachments.local.dir:}")
    public String chatAttachmentsLocalDir;

    @Autowired
    private MSClientHelper msClientHelper;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("index Called");
        // this is a code MS will returns on the AuthController when i receive the
        // Authorization Response
        String state = "my_mapping_id_" + System.currentTimeMillis();
        response.getWriter()
                .println("<html><body>");
        response.getWriter()
                .println("HI BOY!<br>");
        String loginUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize" //
                + "?client_id=" + CLIENT_ID //
                + "&response_type=code" //
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, Charset.forName("utf-8")
                        .name()) //
                + "&response_mode=query" //
                + "&scope=" + URLEncoder.encode(SCOPE, Charset.forName("utf-8")
                        .name()) //
                + "&state=" + state;
        response.getWriter()
                .println("<a href=\"" + loginUrl + "\">login</a>");
        response.getWriter()
                .println("</body></html>");
    }

    @GetMapping(path = "/loggedIn", produces = MediaType.TEXT_HTML_VALUE)
    public void loggedIn(@RequestParam String accessToken, HttpServletResponse response) throws IOException {
        response.getWriter()
                .println("<html><body>");
        response.getWriter()
                .println("<br><br>ACCESS_TOKEN:<br>");
        response.getWriter()
                .println(accessToken);
        JSONObject json = msClientHelper.lowLevelGet("/beta/me", accessToken);
        String myUserId = json.getString("id");
        response.getWriter()
                .println("<!-- ME: ");
        response.getWriter()
                .println(json);
        response.getWriter()
                .println(" -->");

        response.getWriter()
                .println("<br><br>CHATS:<BR>");

        json = msClientHelper.lowLevelGet("/beta/me/chats", accessToken);
        JSONArray chats = json.getJSONArray("value");
        response.getWriter()
                .println("<table BORDER=1 CELLSPACING=0 CELLPADDING=0><tr>" //
                        + "<th>id</th>" //
                        + "<th>topic</th>" //
                        + "<th>lastUpdatedDateTime</th>" //
                        + "<th>chatType</th>" //
                        + "</tr>");
        for (int i = 0; i < chats.length(); i++) {
            JSONObject chatJO = chats.getJSONObject(i);
            String id = chatJO.getString("id");
            String topic = chatJO.optString("topic");
            String lastUpdatedDateTime = chatJO.optString("lastUpdatedDateTime");
            String chatType = chatJO.optString("chatType");
            String href = "/inChat?accessToken=" + accessToken + "&chatId=" + id;
            response.getWriter()
                    .println("<tr>" //
                            + "<td><a href=\"" + href + "\">" + id + "</a></td>" //
                            + "<td>" + topic + "</td>" //
                            + "<td>" + lastUpdatedDateTime + "</td>" //
                            + "<td>" + chatType + "</td>" //
                            + "</tr>");
        }
        response.getWriter()
                .println("</tr></table><br>");

        response.getWriter()
                .println("<br><br>TEAMS ( -&gt; CHANNELS):<BR>");

        // get all teams
        // json = msClientHelper.lowLevelGet("/beta/me/joinedTeams", accessToken);
        // json = msClientHelper.lowLevelGet("/v1.0/groups?
        // $filter=resourceProvisioningOptions/Any(x:x eq 'Team')",accessToken); - needs scope GroupMember.Read.All
        json = msClientHelper.lowLevelGet("/v1.0/users/" + myUserId + "/teamwork/associatedTeams", accessToken);

        JSONArray teams = json.getJSONArray("value");
        response.getWriter()
                .println("<table BORDER=1 CELLSPACING=0 CELLPADDING=0><tr>" //
                        + "<th>id</th>" //
                        + "<th>displayName</th>" //
                        + "<th>tenantId</th>" //
                        + "</tr>");
        for (int i = 0; i < teams.length(); i++) {
            JSONObject teamJO = teams.getJSONObject(i);
            String id = teamJO.getString("id");
            String displayName = teamJO.optString("displayName");
            String tenantId = teamJO.optString("tenantId"); // doing "/associatedTeams" i've tenantId instead of
                                                            // description
            String href = "/inTeam?accessToken=" + accessToken + "&teamId=" + id;
            response.getWriter()
                    .println("<tr>" //
                            + "<td><a href=\"" + href + "\">" + id + "</a></td>" //
                            + "<td>" + displayName + "</td>" //
                            + "<td>" + tenantId + "</td>" //
                            + "</tr>");
        }
        response.getWriter()
                .println("</tr></table><br>");

        // EMAIL FOLDERS
        response.getWriter()
                .println("<br><br>EMAIL FOLDERS:<BR>");
        // get all folders

        MailFolderCollectionPage emailFolders = msClientHelper.getEmailFolders(accessToken);
        response.getWriter()
                .println("<table BORDER=1 CELLSPACING=0 CELLPADDING=0><tr>" //
                        + "<th>id</th>" //
                        + "<th>displayName</th>" //
                        + "<th>count</th>" //
                        + "</tr>");
        List<MailFolder> currentPage = emailFolders.getCurrentPage();
        for (MailFolder folder : currentPage) {
            String id = folder.id;
            String displayName = folder.displayName;
            Integer tot = folder.totalItemCount;
            String href = "/inEmailFolder?accessToken=" + accessToken + "&folderId=" + id;
            response.getWriter()
                    .println("<tr>" //
                            + "<td><a href=\"" + href + "\">" + id + "</a></td>" //
                            + "<td>" + displayName + "</td>" //
                            + "<td>" + tot + "</td>" //
                            + "</tr>");
            // NOTE folder.messages is null. to have messages i've to call another api
        }
        response.getWriter()
                .println("</tr></table><br>");

        response.getWriter()
                .println("</body></html>");
    }

    @GetMapping(path = "/inTeam", produces = MediaType.TEXT_HTML_VALUE)
    public void inTeam(@RequestParam String accessToken, @RequestParam String teamId, HttpServletResponse response)
            throws IOException {
        response.getWriter()
                .println("<html><body>");
        response.getWriter()
                .println("<br><br>CHANNELS:<BR>");

        JSONObject json = msClientHelper.lowLevelGet("/beta/teams/" + teamId + "/channels", accessToken);
        JSONArray channels = json.getJSONArray("value");
        response.getWriter()
                .println("<table BORDER=1 CELLSPACING=0 CELLPADDING=0><tr>" //
                        + "<th>id</th>" //
                        + "<th>displayName</th>" //
                        + "</tr>");
        for (int i = 0; i < channels.length(); i++) {
            JSONObject channelJO = channels.getJSONObject(i);
            String id = channelJO.getString("id");
            String displayName = channelJO.optString("displayName");
            String href = "/inTeamInChannel?accessToken=" + accessToken + "&teamId=" + teamId + "&channelId=" + id;
            response.getWriter()
                    .println("<tr>" //
                            + "<td><a href=\"" + href + "\">" + id + "</a></td>" //
                            + "<td>" + displayName + "</td>" //
                            + "</tr>");
        }
        response.getWriter()
                .println("</tr></table><br>");

        response.getWriter()
                .println("</body></html>");
    }

    @GetMapping(path = "/inEmailFolder", produces = MediaType.TEXT_HTML_VALUE)
    public void inEmailFolder(@RequestParam String accessToken, @RequestParam String folderId,
            HttpServletResponse response) throws IOException {
        response.getWriter()
                .println("<html><body>");
        response.getWriter()
                .println("<br><br>EMAILS:<BR>");

        MessageCollectionPage emails = msClientHelper.getEmails(accessToken, folderId);
        response.getWriter()
                .println("<table BORDER=1 CELLSPACING=0 CELLPADDING=0><tr>" //
                        + "<th>from</th>" //
                        + "<th>to</th>" //
                        + "<th>subject</th>" //
                        + "<th>bodyPreview</th>" //
                        + "</tr>");
        List<Message> currentPage = emails.getCurrentPage();
        for (Message message : currentPage) {

            if (message.hasAttachments) {
                // here message has attachment = null!
                AttachmentCollectionPage emailAttachments = msClientHelper.getEmailAttachments(accessToken, folderId,
                                                                                               message.id);
                logger.info("subject:" + message.subject);
                List<Attachment> att = emailAttachments.getCurrentPage();
                for (Attachment at : att) {
                    // at could be FileAttachment, ItemAttachment or ReferenceAttachment
                    // FileAttachment has contentBytes <-- standard email are like this
                    // ItemAttachment has a property item that is an OutlookItem (contact, event or
                    // message, represented by an itemAttachment resource)
                    // ReferenceAttachment has a sourceUrl
                    logger.info("attachment: " + at.name + " [" + at.contentType + "]");
                }
            }

            // String id = message.id;
            String from = message.from.emailAddress.address;
            List<String> to = new ArrayList<String>();
            message.toRecipients.stream()
                    .forEach(rec -> to.add(rec.emailAddress.address));
            String subject = message.subject;
            String bodyPreview = message.bodyPreview;
            response.getWriter()
                    .println("<tr>" //
                            + "<td>" + from + "</td>" //
                            + "<td>" + to + "</td>" //
                            + "<td>" + subject + "</td>" //
                            + "<td>" + bodyPreview + "</td>" //
                            + "</tr>");
        }
        response.getWriter()
                .println("</tr></table><br>");

        response.getWriter()
                .println("</body></html>");
    }

    @GetMapping(path = "/inChat", produces = MediaType.TEXT_HTML_VALUE)
    public void inChat(@RequestParam String accessToken, @RequestParam String chatId, HttpServletResponse response)
            throws IOException {
        response.getWriter()
                .println("<html><body>");
        response.getWriter()
                .println("<b>" + (new Date()) + " - webhook exp. " + MSClientHelper.WEBHOOK_MINUTES_EXPIRATION
                        + "'</b><br>");
        JSONObject json = msClientHelper.lowLevelGet("/beta/me/chats/" + chatId + "/messages", accessToken);
        JSONArray messages = json.getJSONArray("value");

        // send part
        response.getWriter()
                .println(""//
                        + "<script>\n" //
                        + "function sendMsg() {\n"//
                        + "	var msg_text = document.getElementById('msg_text');\n"//
                        + "    var url = \"/api/chats/send?accessToken=" + accessToken + "&chatId=" + chatId
                        + "&message=\"+encodeURIComponent(msg_text.value);\n"//
                        + "    msg_text.value=\"\";\n"//
                        + "    var xmlHttp = new XMLHttpRequest();\n"//
                        + "    xmlHttp.open(\"GET\", url, true);\n"//
                        + "    xmlHttp.send(null);\n"//
                        + "}\n" + "</script>\n");
        response.getWriter()
                .println("<input type=\"text\" id=\"msg_text\" size=\"20\" name=\"message\" value=\"\"/>");
        response.getWriter()
                .println("<input type=\"button\" value=\"send\" onclick=\"sendMsg();\"/>");
        response.getWriter()
                .println("<hr>");

        // realtime stuff
        if (webhooksEnabled) {
            String streamUrl = "/api/stream/chat?chatId=" + chatId + "&accessToken=" + accessToken;
            response.getWriter()
                    .println("" //
                            + "<script>" //
                            + "        const evtSource = new EventSource('" + streamUrl
                            + "', { withCredentials: false } );\n" //
                            + "        evtSource.onmessage = function(event) {\n" //
                            + "            console.log('got event', event);\n" //
                            + "            var p = document.createElement('p');\n" //
                            + "            var json = JSON.parse(event.data);\n" //
                            + "            p.innerHTML = json.from+\" - \"+json.text;\n" //
                            + "            document.getElementById('realtime').appendChild(p);\n" //
                            + "        }\n" //
                            + "</script>");
            response.getWriter()
                    .println("<div id='realtime'></div>");
            // subscribe to webhook TODO check if not already subscribed + manage expiration
            String webhookResource = "/chats/" + chatId + "/messages";
            try {
                if (msClientHelper.alreadySubscribedToWebhook(webhookResource, accessToken)) {
                    logger.info("already subscribed to webhook");
                    response.getWriter()
                            .println("<br><b>already subscribed to webhook - will i receive notifications?</b>");
                } else {
                    msClientHelper.subscribeToWebhook(webhookResource, accessToken);
                    response.getWriter()
                            .println("<br><b>subscribed to webhook: " + webhookResource + "</b>");
                }
            } catch (Exception e) {
                logger.warn("already subscribed?", e);
                response.getWriter()
                        .println("<br><b>can't subscribe to webhook: " + e.getMessage() + "</b>");
            }
        } else {
            response.getWriter()
                    .println("Webhooks disabled by configuration<br><hr>");
        }
        response.getWriter()
                .println("<br><hr>");

        // message list
        response.getWriter()
                .println("<table BORDER=1 CELLSPACING=0 CELLPADDING=0><tr>" //
                        + "<th>id</th>" //
                        + "<th>createdDateTime</th>" //
                        + "<th>from</th>" //
                        + "<th>body</th>" //
                        + "</tr>");
        for (int i = 0; i < messages.length(); i++) {
            JSONObject msgJO = messages.getJSONObject(i);
            String id = msgJO.getString("id");
            String createdDateTime = msgJO.getString("createdDateTime");
            // sometimes from is null
            JSONObject fromJO = msgJO.optJSONObject("from");
            String from = fromJO != null ? fromJO.getJSONObject("user")
                    .getString("displayName") : "[empty]";
            JSONObject body = msgJO.getJSONObject("body");

            // save chat attachment?
            if (StringUtils.hasText(chatAttachmentsLocalDir)) {
                JSONArray attachments = msgJO.getJSONArray("attachments");
                for (int a = 0; a < attachments.length(); a++) {
                    JSONObject cma = attachments.getJSONObject(a);
                    String filename = cma.getString("name");
                    String attachmentContentUrl = cma.getString("contentUrl");

                    // DLD attachment
                    // get root webUrl
                    JSONObject rootJson = msClientHelper.lowLevelGet("/v1.0/me/drive/root", accessToken);
                    String rootWebUrl = rootJson.getString("webUrl");

                    // root web url is something like:
                    // https://enginius-my.sharepoint.com/personal/alessio_olivieri_enginius_com/Documents
                    // attachment web url is something like:
                    // https://enginius-my.sharepoint.com/personal/alessio_olivieri_enginius_com/Documents/Microsoft
                    // Teams Chat Files/aruco4x4.png
                    // i'll need to compose the drive download query for (file url) = (attachment web url) - (root web
                    // url)
                    // https://graph.microsoft.com/1.0/me/drive/root:(file url) i.e.
                    // https://graph.microsoft.com/1.0/me/drive/root:/Microsoft Teams Chat Files/aruco4x4.png
                    // then i get "@microsoft.graph.downloadUrl" of this returned object
                    if (attachmentContentUrl != null && attachmentContentUrl.startsWith(rootWebUrl)) {
                        String fileUrl = attachmentContentUrl.substring(rootWebUrl.length());
                        if (fileUrl.startsWith("/"))
                            fileUrl = fileUrl.substring(1);
                        JSONObject attachmentJson = msClientHelper.lowLevelGet("/beta/me/drive/root:/" + fileUrl,
                                                                               accessToken);

                        // save attachment
                        logger.info("got attachment " + attachmentJson);
                        URL dldUrl = new URL(attachmentJson.getString("@microsoft.graph.downloadUrl"));
                        File f = new File("/tmp/attachments/");
                        f.mkdirs();
                        InputStream fin = dldUrl.openConnection()
                                .getInputStream();
                        FileOutputStream fout = new FileOutputStream(new File(f, filename));
                        IOUtils.copy(fin, fout);
                        fin.close();
                        fout.close();
                    }
                }
            }
            // display messages
            response.getWriter()
                    .println("<tr>" //
                            + "<td>" + id + "</td>" //
                            + "<td>" + createdDateTime + "</td>" //
                            + "<td>" + from + "</td>" //
                            + "<td>" + body.optString("content") + "</td>" //
                            + "</tr>");
        }
        response.getWriter()
                .println("</table>");

        response.getWriter()
                .println("</body></html>");
    }

    @GetMapping(path = "/inTeamInChannel", produces = MediaType.TEXT_HTML_VALUE)
    public void inTeamInChannel(@RequestParam String accessToken, @RequestParam String teamId,
            @RequestParam String channelId, HttpServletResponse response) throws IOException {
        response.getWriter()
                .println("<html><body>");
        response.getWriter()
                .println("<b>" + (new Date()) + " - webhook exp. " + MSClientHelper.WEBHOOK_MINUTES_EXPIRATION
                        + "'</b><br>");
        JSONObject json = msClientHelper.lowLevelGet("/beta/teams/" + teamId + "/channels/" + channelId + "/messages",
                                                     accessToken);
        JSONArray messages = json.getJSONArray("value");

        // send part
        response.getWriter()
                .println(""//
                        + "<script>\n" //
                        + "function sendMsg() {\n"//
                        + "	var msg_text = document.getElementById('msg_text');\n"//
                        + "    var url = \"/api/channels/send?accessToken=" + accessToken + "&teamId=" + teamId
                        + "&channelId=" + channelId + "&message=\"+encodeURIComponent(msg_text.value);\n"//
                        + "    msg_text.value=\"\";\n"//
                        + "    var xmlHttp = new XMLHttpRequest();\n"//
                        + "    xmlHttp.open(\"GET\", url, true);\n"//
                        + "    xmlHttp.send(null);\n"//
                        + "}\n" + "</script>\n");
        response.getWriter()
                .println("<input type=\"text\" id=\"msg_text\" size=\"20\" name=\"message\" value=\"\"/>");
        response.getWriter()
                .println("<input type=\"button\" value=\"send\" onclick=\"sendMsg();\"/>");
        response.getWriter()
                .println("<hr>");

        // realtime stuff
        if (webhooksEnabled) {
            String streamUrl = "/api/stream/channel?teamId=" + teamId + "&channelId=" + channelId + "&accessToken="
                    + accessToken;
            response.getWriter()
                    .println("" //
                            + "<script>" //
                            + "        const evtSource = new EventSource('" + streamUrl
                            + "', { withCredentials: false } );\n" //
                            + "        evtSource.onmessage = function(event) {\n" //
                            + "            console.log('got event', event);\n" //
                            + "            var p = document.createElement('p');\n" //
                            + "            var json = JSON.parse(event.data);\n" //
                            + "            p.innerHTML = json.from+\" - \"+json.text;\n" //
                            + "            document.getElementById('realtime').appendChild(p);\n" //
                            + "        }\n" //
                            + "</script>");
            response.getWriter()
                    .println("<div id='realtime'></div>");

            // subscribe to webhook TODO check if not already subscribed + manage expiration
            String webhookResource = "/teams/" + teamId + "/channels/" + channelId + "/messages";
            try {
                if (msClientHelper.alreadySubscribedToWebhook(webhookResource, accessToken)) {
                    logger.info("already subscribed to webhook");
                    response.getWriter()
                            .println("<br><b>already subscribed to webhook - will i receive notifications?</b>");
                } else {
                    msClientHelper.subscribeToWebhook(webhookResource, accessToken);
                    response.getWriter()
                            .println("<br><b>subscribed to webhook: " + webhookResource + "</b>");
                }
            } catch (Exception e) {
                logger.warn("already subscribed?", e);
                response.getWriter()
                        .println("<br><b>can't subscribe to webhook: " + e.getMessage() + "</b>");
            }
        } else {
            response.getWriter()
                    .println("Webhooks disabled by configuration<br><hr>");
        }
        response.getWriter()
                .println("<br><hr>");

        // message list
        response.getWriter()
                .println("<table BORDER=1 CELLSPACING=0 CELLPADDING=0><tr>" //
                        + "<th>id</th>" //
                        + "<th>createdDateTime</th>" //
                        + "<th>from</th>" //
                        + "<th>body</th>" //
                        + "</tr>");
        for (int i = 0; i < messages.length(); i++) {
            JSONObject msgJO = messages.getJSONObject(i);
            String id = msgJO.getString("id");
            String createdDateTime = msgJO.getString("createdDateTime");
            // sometimes from is null
            JSONObject fromJO = msgJO.optJSONObject("from");
            String from = fromJO != null ? fromJO.getJSONObject("user")
                    .getString("displayName") : "[empty]";
            JSONObject body = msgJO.getJSONObject("body");

            response.getWriter()
                    .println("<tr>" //
                            + "<td>" + id + "</td>" //
                            + "<td>" + createdDateTime + "</td>" //
                            + "<td>" + from + "</td>" //
                            + "<td>" + body.optString("content") + "</td>" //
                            + "</tr>");
        }
        response.getWriter()
                .println("</table>");

        response.getWriter()
                .println("</body></html>");
    }
}
