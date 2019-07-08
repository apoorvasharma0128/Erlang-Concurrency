import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.Random;
import java.util.ArrayList;

/**
 * The class is responsible for performing the customer operations.
 */
public class Customer implements Runnable {

    protected BlockingQueue<HashMap<String,String>>queue;
    protected BlockingQueue<HashMap<String,String>>bankqueue;
    protected BlockingQueue<String> msgs;
    protected String key;
    protected int value;
    protected int initialvalue;
    protected ArrayList<String> bankSet = new ArrayList<>();
    protected String msg="";
    protected  boolean isRunning=true;
    protected BlockingQueue<Integer> size;
    protected int counter =0;
    HashMap<String,Boolean> responseMap;
    Customer(BlockingQueue<HashMap<String,String>> queue, String key,int value,Set<String> bankSet,
             BlockingQueue<String> msgs,BlockingQueue<Integer> size,BlockingQueue<HashMap<String,String>>bankqueue){
        this.queue =queue;
        this.key=key;
        this.value=value;
        this.bankSet.addAll(bankSet);
        this.initialvalue = value;
        this.msgs =msgs;
        this.size =size;
        this.bankqueue=bankqueue;
        responseMap = new HashMap<String,Boolean>();

    }

    @Override
    public void run() {
        while(isRunning) {
            if (queue.isEmpty() && counter==0){
                responseMap.put(key,false);
                applyLoan();
            }else if(!bankqueue.isEmpty()){
                checkforStatus();
            }
            counter++;
        }

    }

    /**
     * This method is responsible for applying for the loan.
     */
    synchronized private void applyLoan(){
           Boolean bool = responseMap.get(key);
           if(!bool) {
               Random rand = new Random();
               int randomamt = 0;
               if (value < 50) {
                   randomamt = value;
               } else {
                   randomamt = rand.nextInt((50 - 0) + 1) + 0;
               }

               int maxRange = bankSet.size();
               int randindex = rand.nextInt((maxRange - 0) + 1) + 0;
               if (randindex >= maxRange) {
                   randindex = randindex - 1;
               }
               String randomBank = bankSet.get(randindex);
//               if(randomamt>0) {
                   msg = key + " requests a loan of " + randomamt + " dollar(s) from " + randomBank + ".";
                   HashMap<String, String> tempMap = new HashMap<String, String>();
                   tempMap.put(key + ";" + randomBank, "borrow;" + randomamt);
                   queue.add(tempMap);
                   msgs.add(msg);
                   responseMap.put(key, true);
//               }
           }

    }

    /**
     * Responsible for checking banks response and taking necessary action accordingly.
     */
    synchronized private void checkforStatus(){
        for (HashMap<String,String> m :bankqueue){
            for(String s :m.keySet()){
                String[] temp = s.split(";");
                String[] tval = m.get(s).split(";");
                if(temp[0].equalsIgnoreCase(key)){
                    if (!tval[0].equalsIgnoreCase("borrow")){
//                        msg =temp[1]+" "+tval[0]+" a loan of "+tval[1]+" dollar(s) from "+key+".";
//                        msgs.add(msg);
                        responseMap.put(key,false);
                        bankqueue.remove(m);
                        reApplyLoan(tval[0].trim(),Integer.parseInt(tval[1].trim()),temp[1]);
                    }
                }

            }

        }

    }

    /**
     * Responsible for suspending the thread execution.
     */
    synchronized public void stopThread() {
       size.poll();
        this.isRunning = false;
    }

    /**
     * Responsile for re-applying for loan
     * @param status
     * @param amount
     * @param bankname
     */
    synchronized private void reApplyLoan(String status,int amount,String bankname){
        if(status.equalsIgnoreCase("denied")){
            bankSet.remove(bankname);
        }else{
            value =value - amount;
        }
//        System.out.println("Customer Value"+value);
        if(value==0){
            msg =key+" has reached the objective of "+initialvalue+" dollar(s). Woo Hoo!";
            msgs.add(msg);
            isRunning =false;
            stopThread();
        }
        else if(bankSet.size()==0){
            int borrowedamt = initialvalue-value;
            msg =key+" was only able to borrow "+borrowedamt+" dollar(s). Boo Hoo!";
            stopThread();
            msgs.add(msg);
        }else {
            applyLoan();
        }


    }
}
