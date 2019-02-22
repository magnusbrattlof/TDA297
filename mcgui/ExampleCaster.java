
import mcgui.*;
import java.util.*;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleCaster extends Multicaster {

    int seqNum;
    int globalSeq;
    int localSeq;
    int proposedSeq;

    ArrayList<ExampleMessage> hold_back_queue;
    HashMap<Integer, List<Integer>> received;
    ArrayList<ArrayList<Integer>> rcvd_proposals;

    /**
     * No initializations needed for this simple one
     */
    public void init() {

        hold_back_queue = new ArrayList<ExampleMessage>();
        received = new HashMap<Integer, List<Integer>>();

        for(int i = 0; i < hosts; i++) {
            received.put(i, new ArrayList<Integer>());
        }

        rcvd_proposals = new ArrayList<ArrayList<Integer>>();
        proposedSeq = 0;
        globalSeq = 0;
        localSeq = 0;
        seqNum = 0;
    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        ExampleMessage msg = new ExampleMessage(id, messagetext, seqNum, 1, proposedSeq, id, 0);

        for(int i=0; i < hosts; i++) {
            /* Sends to everyone except itself */
            if(i != id) {
                bcom.basicsend(i, msg);
            }
        }

        seqNum++;
        rcvd_proposals.add(new ArrayList<Integer>());
    }

    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer, Message message) {
        ExampleMessage msg = (ExampleMessage)message;

        if(msg.phase == 1) {
            // Check if message is not received or if received is empty.
            if(is_received(msg)) {
                //mcui.debug("Message " +msg.seqNum+ " from " +msg.getSender()+ " already received");
            }
            // If not, we have already received this message
            else {

                // Add message to received
                add_to_received(msg);
                // Reliable broadcast to others
                rel_bcast(peer, msg);
                // Send back proposed value to initiatior
                propose(msg.getSender(), msg);

                //mcui.debug("Peer: " + msg.getSender() + " sequence: " + received.get(peer).get(msg.seqNum));
            }
        }

        else if(msg.phase == 2) {

            
            rcvd_proposals.get(msg.seqNum).add(msg.proposed);

            // If we have received all the proposals
            if(rcvd_proposals.get(msg.seqNum).size() == (hosts - 1)) {

                // Set the global sequence id to the maximum proposed
                globalSeq = Collections.max(rcvd_proposals.get(msg.seqNum));
                rcvd_proposals.get(msg.seqNum).clear();
                //mcui.debug("I choose: "+globalSeq);
                // Initiate phase three
                msg.phase = 3;
                msg.globalSeq = globalSeq;
                rel_bcast(id, msg);

                // Add the message to the hold_back_queue
                //hold_back_queue.put(globalSeq, msg);
                pre_deliver(msg, globalSeq);
                //mcui.debug("Sender: "+msg.getSender() + " globalSeq: "+globalSeq);
            }
        }

        else if(msg.phase == 3) {
            globalSeq = Math.max(msg.globalSeq, globalSeq);
            msg.globalSeq = globalSeq;
            //mcui.debug("We consent on: "+globalSeq);

            // Add the message to the hold_back_queue
            //hold_back_queue.put(globalSeq, msg);
            pre_deliver(msg, globalSeq);
            //mcui.debug("Sender: " +msg.getSender() + " globalSeq: " + globalSeq);

        }

        else {
            mcui.debug("Mmmm sad face");
        }
    }
        //mcui.deliver(peer, msg.text);

    public void propose(int peer, Message msg) {
        ExampleMessage new_msg = (ExampleMessage)msg;

        // Propose a sequence id to the received message from peer
        proposedSeq = Math.max(proposedSeq, globalSeq) + 1;
        new_msg.proposed = proposedSeq;

        // Set the proposer so that peer knows who proposed
        new_msg.proposer = id;

        //mcui.debug("Proposed: "+new_msg.proposed);

        // Set phase to two
        new_msg.phase = 2;
        // Send back the proposed sequence id
        bcom.basicsend(peer, new_msg);
    }

    // Peer is used for checking so that we dont send back to the flooding peer
    public void rel_bcast(int peer, ExampleMessage msg) {
        for(int i = 0; i < hosts; i++) {
            // Do not send to yourself and the peer you received from
            if(i != peer && i != id && i != msg.getSender()) {
                bcom.basicsend(i, msg);
            }
        }
    }

    public void pre_deliver(ExampleMessage msg, int seq) {

        hold_back_queue.add(msg);
        Collections.sort(hold_back_queue);

        System.out.println("------ JAG ÄR ID: " + id + " -------");
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();


        try {
            for (int i = 1; i < hold_back_queue.size(); i++) {
                if (hold_back_queue.get(i - 1).globalSeq != seq){
                    mcui.deliver(hold_back_queue.get(i - 1).getSender(), Integer.toString(hold_back_queue.get(i - 1).seqNum));   //.getText());
                    hold_back_queue.remove(i-1);
                }
            }
        } catch(Exception e) {
            //lol
        }  
    //    mcui.debug("Sender: " + hold_back_queue.get(i).getSender() + " msg seq: " + hold_back_queue.get(i).seqNum + " agreed sequence: " + seq);
        
    }

    public void add_to_received(ExampleMessage msg) {
        received.get(msg.getSender()).add(msg.seqNum);
    }

    public boolean is_received(ExampleMessage msg) {
        int msg_sender = msg.getSender();
        int msg_seqnum = msg.seqNum;

        if(!(received.get(msg_sender).contains(msg_seqnum)) || received.get(msg_sender).isEmpty()) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Signals that a peer is down and has been down for a while to
     * allow for messages taking different paths from this peer to
     * arrive.
     * @param peer	The dead peer
     */
    public void basicpeerdown(int peer) {
        mcui.debug("Peer "+peer+" has been dead for a while now!");
    }
}
