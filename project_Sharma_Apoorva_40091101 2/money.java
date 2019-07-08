import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * This class is the main driver class. It creates the bank and customer threads and displays the
 * result of operation performed by individual threads.
 */
public class money {
    /**
     * @param argv
     */
    public static void main(String[] argv){
        try{
            BlockingQueue<HashMap<String,String>> queue = new LinkedBlockingDeque<>();
            BlockingQueue<String> msgs = new LinkedBlockingDeque<>();
            BlockingQueue<Integer> size = new LinkedBlockingDeque<>();
            BlockingQueue<HashMap<String,String>>bankqueue = new LinkedBlockingDeque<>();
            ReadFile rf= new ReadFile();
            rf.readFile();
            int tPoolSize=rf.getBankRecords().size()+rf.getCustomerRecords().size();
            ExecutorService executor = Executors.newFixedThreadPool(tPoolSize);
            Map<String,Integer> bank = rf.getBankRecords();
            Map<String,Integer> cust = rf.getCustomerRecords();
            Set<String> bankSet=bank.keySet();
            Set<String> custSet=cust.keySet();
            System.out.println("** Customers and loan objectives **");
            for(String s :custSet){
                System.out.println(s+" : "+cust.get(s));

            }
            System.out.println();
            System.out.println("** Banks and financial resources **");
            for(String s :bankSet){
                System.out.println(s+" : "+bank.get(s));

            }
            System.out.println();
            for(String key: cust.keySet()){
                size.add(1);
                executor.execute(new Customer(queue,key,cust.get(key),bankSet,msgs,size,bankqueue));
            }
            for(String key: bank.keySet()){
                executor.execute(new Bank(queue,key,bank.get(key),size,msgs,bankqueue,custSet));
            }
            executor.shutdown();
            while(true){
                if(!msgs.isEmpty()){
                    String msg =msgs.poll();
                    System.out.println(msg);
                }
            }
        }catch (FileNotFoundException fe){
            System.out.println("File not found!!");

        }

    }
}
