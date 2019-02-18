
import mcgui.*;
import java.util.*;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleCaster extends Multicaster {

    int seqNum;
    HashMap<Integer, List<Integer>> received;

    /**
     * No initializations needed for this simple one
     */
    public void init() {
        received = new HashMap<Integer, List<Integer>>();
        for(int i = 0; i < hosts; i++) {
            received.put(i, new ArrayList<Integer>());
        }

        int seqNum = 0;
    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        ExampleMessage msg = new ExampleMessage(id, messagetext, seqNum);

        for(int i=0; i < hosts; i++) {
            /* Sends to everyone except itself */
            if(i != id) {
                bcom.basicsend(i, msg);
            }
        }
        seqNum++;

        mcui.debug("Sent out: \""+messagetext+"\"");
        mcui.deliver(id, messagetext, "from myself!");
    }

    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer, Message message) {
        ExampleMessage msg = (ExampleMessage)message;

        if(received.get(msg.getSender()).contains(msg.seqNum)) {
            mcui.debug("Message " +msg.seqNum+ " from " +msg.getSender()+ " already received");
        }
        else {
            received.get(msg.getSender()).add(msg.seqNum);
            rel_bcast(peer, msg);
            mcui.debug("Received: " + received.get(peer).get(msg.seqNum));
        }
    }
        //mcui.deliver(peer, msg.text);

    public void rel_bcast(int peer, ExampleMessage msg) {
        for(int i = 0; i < hosts; i++) {
            // Do not send to yourself and the peer you received from
            if(i != peer && i != id && i != msg.getSender()) {
                bcom.basicsend(i, msg);
            }
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
