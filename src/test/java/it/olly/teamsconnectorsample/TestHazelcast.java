package it.olly.teamsconnectorsample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.hazelcast.collection.IQueue;
import com.hazelcast.collection.ItemEvent;
import com.hazelcast.collection.ItemListener;
import com.hazelcast.core.HazelcastInstance;

@SpringBootTest
public class TestHazelcast {

	@Autowired
	private HazelcastInstance hazelcast;

	@Test
	void simpleTestHazelcast() {
		System.out.println("simpleTestHazelcast");
		IQueue<Object> queue = hazelcast.getQueue("myQueue-test");
		queue.addItemListener(new ItemListener<Object>() {

			@Override
			public void itemRemoved(ItemEvent<Object> item) {
				System.out.println("itemRemoved: " + item);

			}

			@Override
			public void itemAdded(ItemEvent<Object> item) {
				System.out.println("itemAdded: " + item);
				queue.remove(item.getItem());
			}
		}, true);
		queue.add("ciao numero uno");
		queue.add("ciao numero due");
		System.out.println("simpleTestHazelcast - DONE");
	}

}
