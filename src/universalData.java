import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jude on 9/11/16.
 */
public class universalData {
    public static JSONObject jsonObject = new JSONObject();









    public static void addDataString(String index, int value)
    {
        try {
            jsonObject.put(index,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static int getRowNo(String index)
    {
        int rowNo=-1;
        try {
            rowNo = jsonObject.getInt(index);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    return rowNo;
    }

    /* Debug Function */
    public static void printData()
    {
        System.out.println("UNIVERSAL DATA"+jsonObject.toString());
    }

}





