package com.example.snjdeveloper.extra;

import android.util.Log;
import android.widget.AutoCompleteTextView;

import com.example.snjdeveloper.notification.NotificationAction;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parameters extends NotificationAction {
    ArrayList<HashMap<String,Object>> parametersMap=new ArrayList<>();
    public Parameters(){
        setAllNotificationOptionsParameters();
    }
    public  Parameters(boolean action){
        setAllActionOptionsParameters();
    }
   public ArrayList<HashMap<String, Object>> getNotificationParametersList(int index){
        return mapToList(parametersMap.get(index));
    }
    public HashMap<String, Object> getNotificationParameters(int index){
        return parametersMap.get(index);
    }
    public ArrayList<HashMap<String, Object>> mapToList(HashMap<String, Object> paraMap,HashMap<String,Object> ...extraKey) {
        ArrayList<HashMap<String,Object>>data=new ArrayList<>();
        Log.e("mapToList","Received "+paraMap);
        for(Map.Entry<String,Object>record:paraMap.entrySet()){
            HashMap<String,Object>map=new HashMap<>();
            map.put("key",record.getKey());
            Object value=record.getValue();
            if (value instanceof Boolean)
            {   if ((boolean)value)
                    map.put("required",true);
                map.put("value","");
            }else map.put("value",value);
            if (extraKey.length!=0)
                map.put("temp_key",extraKey[0].get(record.getKey()));
            data.add(map);
        }
        Log.e("mapToList","Send "+data);
        return data;
    }

    private void setAllNotificationOptionsParameters(){
        parametersMap.add(getInfoBoxParameters());
        parametersMap.add(getBottomBarParameters());
        parametersMap.add(getNotificationDialogParameters());
        parametersMap.add(getWebDialogParameters());
        parametersMap.add(getWebScreenParameters());
    }
    private HashMap<String,Object> addToHash(Object data){
        HashMap<String,Object>map=new HashMap<>();
        map.put("data",data);
        return map;
    }
   private String[] allParameters={"INFO_BOX","BOTTOM_BAR","NOTIFICATION_DIALOG","WEB_DIALOG","WEB_SCREEN"};
    public String[] getAllNotifications(){
        return allParameters;
    }
    private HashMap<String,String>parameters=setParameters(allParameters);
    public String getParameterName(String index){
        String name=parameters.get(index);
        if (name==null)
            name="Invalid";
        return name;
    }
    private HashMap<String, String> setParameters(String[] allParameters){
        HashMap<String,String>map=new HashMap<>();
        int index=0;
        for (String para:allParameters)
        {
            map.put(""+index,para);
            index++;
        }
        return map;
    }

    private String[] infoBoxType={"BIG_BOTTOM_BOX","SMALL_FIXED_BOX"};
    private HashMap<String,String>infoBoxTypeParameters=setParameters(infoBoxType);


    private HashMap<String,Object> getBottomBarParameters(){
        HashMap<String,Object>map=new HashMap<>();
        map.put("ACTION",false);
        map.put("ICON",false);
        map.put("TITLE",true);
        map.put("DESC",true);
        map.put("IMG",false);
        map.put("ACTION_TEXT",false);
        map.put("STYLE",BOTTOM_BAR);
        return map ;
    }
    private HashMap<String,Object> getInfoBoxParameters(){
        HashMap<String,Object>map=new HashMap<>();
        map.put("ACTION",false);
        map.put("TITLE",true);
        map.put("DESC",true);
        map.put("TEXT_COLOR_TITLE",false);
        map.put("TEXT_COLOR_DESC",false);
        map.put("IMG",false);
        map.put("COLOR",false);
        map.put("STYLE",INFO_BOX);
        map.put("STYLE2",0);
        return map;
    }

//    private HashMap<String,Object> getFixedInfoBoxParameters(){
//       return getInfoBoxParameters(-1);
//    }


    private HashMap<String,Object> getNotificationDialogParameters(){
        HashMap<String,Object>map=new HashMap<>();
        map.put("ACTION",false);
        map.put("TITLE",true);
        map.put("DESC",true);
        map.put("ACTION_TEXT",false);
        map.put("STYLE",NOTIFICATION_DIALOG);
        return map;
    }
    private HashMap<String,Object> getWebDialogParameters(){
        HashMap<String,Object>map=new HashMap<>();
        map.put("ACTION",false);
        map.put("HTML",true);
        map.put("WIDTH",true);
        map.put("HTML_WIDTH",true);
        map.put("HTML_HEIGHT",true);
        map.put("STYLE",WEB_DIALOG);
        return map;
    }
    public HashMap<String,Object> getWebScreenParameters(){
        HashMap<String,Object>map=new HashMap<>();
        map.put("ACTION","ACTION_OPEN_WEB");
        map.put("HTML",true);
        map.put("TITLE",false);
        map.put("STYLE",WEB_SCREEN);
        map.put("intent","ACTION_OPEN_WEB");
        return map;
    }
    public HashMap<String,Object> getUpdateScreenParameters(){
        HashMap<String,Object>map=new HashMap<>();
        map.put("ACTION","ACTION_OPEN_UPDATE");
        map.put("intent","ACTION_OPEN_UPDATE");
        map.put("APP_NAME",true);
        map.put("FORCE_UPDATE",true);
        map.put("APP_SIZE",true);
        map.put("SHOW",true);
        map.put("APP_VERSION",true);
        return map;
    }

    public void removeFields(ArrayList<HashMap<String, Object>> list,String[] fields) {
        Log.e("Action","Original data\n"+list);
        List<String> removeList= Arrays.asList(fields);
        int index=0;
        List<Integer> actionIndex=new ArrayList<>();
        List<HashMap<String,Object>>removeData=new ArrayList<>();
        for (HashMap<String,Object> map:
             list) {
            if (removeList.contains((String) map.get("key"))
            //        .equals("ACTION")
            ){
                actionIndex.add(index);
            }else{
               try {
                   if (map.get("value_edittext")!=null)
                       if (map.get("value_edittext") instanceof AutoCompleteTextView)
                           map.put("value",((AutoCompleteTextView) map.get("value_edittext")).getText() );
                       else map.put("value",((TextInputEditText) map.get("value_edittext")).getText() );
               }catch (Exception e ){e.printStackTrace();}
            }
            index++;
        }
        Log.e("RemoveFields","Fields given "+removeList+" data "+list+" indexes found "+actionIndex);
        if (actionIndex.size()!=0) {
            for (int position:actionIndex)
              removeData.add(list.get(position));
            //Object remove = list.remove(actionIndex);
//            if (remove != null)
//                Log.e("Action", "Action removed " + actionIndex);
//            else Log.e("Action", "Action not removed " + actionIndex);
        }
        list.removeAll(removeData);
    }

    public String[] getAllBottomStyles() {
        return infoBoxType;
    }

    ArrayList<HashMap<String,Object>> actionParametersMap=new ArrayList<>();


    public HashMap<String,Object> getInfoActivityParameters(){
        HashMap<String,Object>map=new HashMap<>();
        map.put("intent","ACTION_OPEN_INFO");
        map.put("ACTION","ACTION_OPEN_INFO");
        map.put("1",true);//orderno
        map.put("2",true);//time
        map.put("3",true);//quantity
        map.put("4",true);//note
        map.put("5",true);//amount
        map.put("6",true);//via
        map.put("7",true);//paid_amount
        return map;
    }
    private String[] infoTempParameters={"Id","Time","Quantity","Note","Amount","Paid via","Paid amount"};
    public HashMap<String, Object> mapInfoWithTempParas(){
        HashMap<String,Object>map=new HashMap<>();
        map.put("intent","intent");
        map.put("ACTION","ACTION");
        map.put("1",infoTempParameters[0]);//orderno
        map.put("2",infoTempParameters[1]);//time
        map.put("3",infoTempParameters[2]);//quantity
        map.put("4",infoTempParameters[3]);//note
        map.put("5",infoTempParameters[4]);//amount
        map.put("6",infoTempParameters[5]);//via
        map.put("7",infoTempParameters[6]);//paid_amount
        return map;
    }
    public void replaceInfoParaWithTemp(ArrayList<HashMap<String,Object>> data){
        HashMap<String,Object>map=mapInfoWithTempParas();
        for (HashMap<String,Object>tempMap:data){
            String key= (String) tempMap.get("key");
            if (key!=null) {
                String value = (String) map.get(key);
                if (value != null)
                    tempMap.put("temp_key", map.get(value));
            }
        }
    }
    public HashMap<String,Object> getSettingsActivityParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("intent", "ACTION_OPEN_SETTING");
        map.put("ACTION", "ACTION_OPEN_SETTING");
        return map;
    }
    public HashMap<String,Object> getTransactionActivityParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("intent", "ACTION_OPEN_TRANSACTIONS");
        map.put("ACTION", "ACTION_OPEN_TRANSACTIONS");
        map.put("CLASS_NAME", "Transactions");
        return map;
    }
    public HashMap<String,Object> getOrderActivityParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("intent", "ACTION_OPEN_ORDERS");
        map.put("ACTION", "ACTION_OPEN_ORDERS");
        map.put("CLASS_NAME", "Orders");
        return map;
    }
    public HashMap<String,Object> getBottlesActivityParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("intent", "ACTION_OPEN_BOTTLES");
        map.put("ACTION", "ACTION_OPEN_BOTTLES");
        return map;
    }
    public HashMap<String,Object> getReportParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("intent", "ACTION_OPEN_REPORT");
        map.put("ACTION", "ACTION_OPEN_REPORT");
        return map;
    }
    public HashMap<String,Object> getLoginActivityParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("intent", "ACTION_LOGIN");
        map.put("ACTION", "ACTION_LOGIN");
        return map;
    }
    public HashMap<String,Object> getUserInfoActivityParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("intent", "ACTION_OPEN_USER_INFO");
        map.put("ACTION", "ACTION_OPEN_USER_INFO");
        return map;
    }
    public HashMap<String,Object> getChangeMobileActivityParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("intent", "ACTION_CHANGE_MOBILE");
        map.put("ACTION", "ACTION_CHANGE_MOBILE");
        map.put("EXTRA","true");
        return map;
    }
    public ArrayList<HashMap<String, Object>> getActionParametersList(int index){
        return mapToList(actionParametersMap.get(index));
    }
    public HashMap<String, Object> getActionParameters(int index){
        return actionParametersMap.get(index);
    }
    private void setAllActionOptionsParameters(){
        actionParametersMap.add(getInfoActivityParameters());
        actionParametersMap.add(getUpdateScreenParameters());
        actionParametersMap.add(getSettingsActivityParameters());
        actionParametersMap.add(getTransactionActivityParameters());
        actionParametersMap.add(getOrderActivityParameters());
        actionParametersMap.add(getBottlesActivityParameters());
        actionParametersMap.add(getUserInfoActivityParameters());
        actionParametersMap.add(getReportParameters());
        actionParametersMap.add(getWebScreenParameters());
        actionParametersMap.add(getChangeMobileActivityParameters());
        actionParametersMap.add(getLoginActivityParameters());
        actionParametersMap.add(new HashMap<>());
    }
    private String[] allActionParameters={"Info Screen","Update screen","Open settings","Open transactions",
            "Open orders","Open bottles","Open user info","Open report problem","Open website","Open change mobile",
            "Open login","None"};
    public String[] getAllActions(){
        return allActionParameters;
    }
    public ArrayList<HashMap<String, Object>> getInfoType() {
        ArrayList<HashMap<String,Object>>list=new ArrayList<>();
        HashMap<String,Object>map=new HashMap<>();
        map.put("key","type");
        map.put("value",true);
        list.add(map);
        return list ;
    }
    public String[] getInfoTypeList() {
        return new String[]{"Orders","Transactions","Bottles"} ;
    }
    public HashMap<String, Object> getInitialInfoParameters() {
        HashMap<String,Object>map=new HashMap<>();
        map.put("key","1");
        map.put("temp_key",infoTempParameters[0]);
        map.put("value",true);
        return map ;
    }
    public String[] getAllInfoParameters(){
        return new String[]{"1","2","3","4","5","6","7"};
    }
}
