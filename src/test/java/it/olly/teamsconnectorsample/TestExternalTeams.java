package it.olly.teamsconnectorsample;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.olly.teamsconnectorsample.service.ms.MSClientHelper;

@SpringBootTest
public class TestExternalTeams {
    public final String accessToken = "eyJ0e...";

    @Autowired
    private MSClientHelper msClientHelper;

    @Test
    void listChannels() throws JSONException {
        System.out.println("doing listChannels");

        // https://graph.microsoft.com/v1.0/groups?$filter=groupTypes/any(c:c+eq+'Unified')&$select=id,displayName,mail,description
        // https://graph.microsoft.com/v1.0/groups?$filter=resourceProvisioningOptions/Any(x:x eq 'Team')
        // https://graph.microsoft.com/v1.0/me/memberOf
        // JSONObject json = msClientHelper.lowLevelGet("/v1.0/users/" + myUserId + "/teamwork/associatedTeams",
        // accessToken);
        JSONObject json = msClientHelper.lowLevelGet("/v1.0/me/teamwork/associatedTeams", accessToken);
        System.out.println("allTeams\n" + json);
        JSONArray teams = json.getJSONArray("value");
        for (int i = 0; i < teams.length(); i++) {
            JSONObject teamJO = teams.getJSONObject(i);
            System.out.println("TEAM - " + teamJO);
            /*String teamId = teamJO.getString("id");
            json = msClientHelper.lowLevelGet("/v1.0/teams/" + teamId + "/channels", accessToken);
            JSONArray channels = json.getJSONArray("value");
            for (int j = 0; j < channels.length(); j++) {
                JSONObject channelJO = channels.getJSONObject(j);
                System.out.println("     - CHANNEL - " + channelJO);
            }
            */
        }

        System.out.println("listChannels done");
    }
}
