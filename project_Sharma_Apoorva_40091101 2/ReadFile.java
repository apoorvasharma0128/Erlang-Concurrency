import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**+
 * Responsible for reading bank and customer file.
 */
public class ReadFile {

    ReadFile() {
        customerMap = new HashMap<String, Integer>();
        bankMap = new HashMap<String, Integer>();
    }
    private String custormerFile = "customers.txt";
    private String bankFile = "banks.txt";

    private Map<String, Integer> customerMap;
    private Map<String, Integer> bankMap;


    public void readFile() throws FileNotFoundException {


        try {

            Scanner sc = new Scanner(new File(custormerFile));
            String tempStr = null;
            while (sc.hasNext()) {
                tempStr = sc.nextLine().trim();
                tempStr=tempStr.replace("{", "");
                tempStr=tempStr.replace("}", "");
                tempStr=tempStr.replace(".", "").strip();
                String[] temp = tempStr.split(",");
                customerMap.put(temp[0].trim(), Integer.parseInt(temp[1].trim()));
            }
            sc.close();
        } catch (Exception e) {
            throw e;
        }

        // reading bank file

        try {

            Scanner sc = new Scanner(new File(bankFile));
            String tempStr = null;
            while (sc.hasNext()) {
                tempStr = sc.nextLine().trim();
                tempStr=tempStr.replace("{", "");
                tempStr=tempStr.replace("}", "");
                tempStr=tempStr.replace(".", "").strip();
                String[] temp = tempStr.split(",");
                bankMap.put(temp[0].trim(), Integer.parseInt(temp[1].trim()));
            }
            sc.close();

        } catch (Exception e) {
            throw e;
        }

    }

    public  Map<String, Integer> getCustomerRecords() {
        return customerMap;
    }

    public  Map<String, Integer> getBankRecords() {
        return bankMap;
    }
}
