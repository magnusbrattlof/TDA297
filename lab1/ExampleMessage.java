
import mcgui.*;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleMessage extends Message {
        
    private String text;
    private int vectorValue;
        
    public ExampleMessage(int sender,String text, int vectorValue) {
        super(sender);
        this.text = text;
        this.vectorValue = vectorValue;
    }
    
    /**
     * Returns the text of the message only. The toString method can
     * be implemented to show additional things useful for debugging
     * purposes.
     */
    public String getText() {
        return text;
    }
    
    /**
     * Returns vectorVlaue of message, this info together with id of
     * sender complets all info needed for vector clock 
     */
    public int getVectorValue() {
        return vectorValue;
    }

    public static final long serialVersionUID = 0;
}
