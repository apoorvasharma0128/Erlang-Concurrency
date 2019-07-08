import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * The class is responsible for responding to customers loan request.
 */
public class Bank implements Runnable {
    protected BlockingQueue<HashMap<String,String>> queue;
    protected BlockingQueue<HashMap<String,String>>bankqueue;
    protected String key;
    protected int value;
    protected int intialValue;
    protected String msg="";
    protected BlockingQueue<Integer> size;
    protected BlockingQueue<String> msgs;
    ArrayList<String> custList=new ArrayList<>();
    protected  boolean isRunning=true;
    public Bank(BlockingQueue<HashMap<String,String>> queue, String key, int value,
                BlockingQueue<Integer> size, BlockingQueue<String> msgs,
                BlockingQueue<HashMap<String,String>>bankqueue, Set<String> custList){
        this.queue =queue;
        this.key=key;
        this.value=value;
        this.intialValue=value;
        this.size =size;
        this.msgs=msgs;
        this.bankqueue=bankqueue;
        this.custList.addAll(custList);
    }
    @Override
    public void run() {
        while(isRunning){
            if (!queue.isEmpty()) {
                processLoan();
            }

            if(custList.isEmpty()){
                isRunning=false;
                msg =key+" has "+value+" dollar(s) remaining.";
                msgs.add(msg);
                stopThread();
            }

            if(size.isEmpty() && isRunning){
                msg =key+" has "+value+" dollar(s) remaining.";
                msgs.add(msg);
                stopThread();
            }

        }

    }

    /**
     * Based on the data present in queue, responds to the customer request.
     */
    synchronized private void processLoan(){
        HashMap<String,String> tempMap =new HashMap<String,String>();
        for(HashMap<String,String> m : queue){
            for(String s :m.keySet()) {
                String[] temp = s.split(";");
                String[] tval = m.get(s).split(";");
                if (temp[1].equalsIgnoreCase(key)) {
                    if (tval[0].equalsIgnoreCase("borrow")){
                        String status="";
                        if (Integer.parseInt(tval[1])==0){
                            status="denied";
                            tempMap =m;
                        }
                        if (Integer.parseInt(tval[1])<=value){
                            value =value - Integer.parseInt(tval[1]);
                            status="approved";
                            tempMap =m;

                        }else{
                            status="denied";
                            tempMap =m;
                            custList.remove(temp[0]);
                        }
                        process(temp[0],status,tempMap);
                    }

                }
            }
        }

    }

    /**
     *
     * @param cust
     * @param status Status of the customer request i.e. approved or denied
     * @param toRemove Request of the customer
     */
    synchronized private void process(String cust,String status,HashMap<String,String> toRemove){
        queue.remove(toRemove);
        String[] tval = toRemove.get(cust+";"+key).split(";");
        toRemove.put(cust+";"+key,status+";"+tval[1]);
         msg =key+" "+status+" a loan of "+tval[1]+" dollar(s) from "+cust+".";
         msgs.add(msg);
         bankqueue.add(toRemove);


    }

    /**
     * This method is responsible for suspending the thread execution.
     */
    synchronized public void stopThread() {
       this.isRunning = false;
    }
}
