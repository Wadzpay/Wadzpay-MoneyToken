package com.wadzpay.ddflibrary.api;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.wadzpay.ddflibrary.models.ApiDataModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ApiJsonParsing {
    private Context mContext;
    private Activity mActivity;
    /*JsonParsing(Context context){
        mContext = context;
        mActivity = (Activity) mContext;
    }*/
    private static ApiJsonParsing instance = null;
    private ApiJsonParsing() {}

    public static ApiJsonParsing getInstance() {
        if(instance == null) {
            instance = new ApiJsonParsing();
        }
        return instance;
    }

    private JSONObject jsonResponse;

    public void startParsing(String jsonString) {
        try {
            jsonResponse = (new JSONObject(jsonString));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String parseKey(String jsonKey) {
        try {
            return jsonResponse.getString(jsonKey) + "";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String arrayKey = "", objectKey = "";

    public void parseJsonDynamic(JSONObject data) {

        if (data != null) {
            Iterator<String> it = data.keys();
            while (it.hasNext()) {
                String key = it.next();
                try {
                    if (data.get(key) instanceof JSONArray) {
                        JSONArray jsonArray = data.getJSONArray(key);
                        int size = jsonArray.length();
                        for (int i = 0; i < size; i++) {
                            arrayKey = key + "";
                            parseHashMapTwoKey(jsonArray.getJSONObject(i));
                        }
                    } else if (data.get(key) instanceof JSONObject) {
                        objectKey = key + "";
                        parseHashMapTwoKey(data.getJSONObject(key));
                    } else {
                    }
                } catch (Throwable e) {
                    try {
                    } catch (Exception ee) {
                    }
                    e.printStackTrace();
                }
            }
        }
    }

    //     dynamic hashmap
    public ArrayList<HashMap<String, String>> parseJsonHashMapDynamic(JSONObject data) {
        ArrayList<HashMap<String, String>> dynamicHashMap = new ArrayList<>();
        HashMap<String, String> contact = new HashMap<>();
        if (data != null) {
            Iterator<String> it = data.keys();
            while (it.hasNext()) {
                String key = it.next();
                try {
                    if (data.get(key) instanceof JSONArray) {
                        JSONArray jsonArray = data.getJSONArray(key);
                        int size = jsonArray.length();
                        for (int i = 0; i < size; i++) {
                            arrayKey = key + "";
//                            parseHashMapTwoKey(jsonArray.getJSONObject(i));
                            getArrayDataNew(jsonArray.getJSONObject(i), arrayKey);
                        }
                    } else if (data.get(key) instanceof JSONObject) {
                        objectKey = key + "";
//                        parseHashMapTwoKey(data.getJSONObject(key));
                    } else {
//                    JSONObject jsonObject =  jsonArray.getJSONObject(i);
//                    String id = jsonObject.getString("id");
//                    contact.put(key,)
                    }
                } catch (Throwable e) {
                    try {
                    } catch (Exception ee) {
                    }
                    e.printStackTrace();
                }
            }
        }
        return dynamicHashMapNew;
    }

    private HashMap<String, String> contactNew = new HashMap<>();
    private ArrayList<HashMap<String, String>> dynamicHashMapNew = new ArrayList<>();

    private void getArrayDataNew(JSONObject jsonObject, String arrayKey) {
        try {
            String valueNew = jsonObject.getString(arrayKey);
            contactNew.put(arrayKey, valueNew);
            dynamicHashMapNew.add(contactNew);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    //    json dynamic with two key hashmap
    private HashMap<String, HashMap<String, String>> hMapTwoKey = new HashMap<>();
    private HashMap<String, String> hashMapSecond = new HashMap<>();

    public HashMap<String, HashMap<String, String>> parseJsonDynamicHashMapTwoKey(JSONObject data) {
        if (data != null) {
            Iterator<String> it = data.keys();
            while (it.hasNext()) {
                String key = it.next();
                try {
                    if (data.get(key) instanceof JSONArray) {
                        JSONArray jsonArray = data.getJSONArray(key);
                        int size = jsonArray.length();
                        for (int i = 0; i < size; i++) {
                            arrayKey = key + "";
                            parseHashMapTwoKey(jsonArray.getJSONObject(i));
                        }
                    } else if (data.get(key) instanceof JSONObject) {
                        objectKey = key + "";
                        parseHashMapTwoKey(data.getJSONObject(key));
                    } else {
                    }
                } catch (Throwable e) {
                    try {
                    } catch (Exception ee) {
                    }
                    e.printStackTrace();
                }
            }
        }
        return hMapTwoKey;
    }

    // hashmap parsing
    public void parseHashMapTwoKey(JSONObject data) {

        if (data != null) {
            Iterator<String> it = data.keys();
            while (it.hasNext()) {
                String key = it.next();
                try {
                    if (data.get(key) instanceof JSONArray) {
                        JSONArray jsonArray = data.getJSONArray(key);
                        int size = jsonArray.length();
                        for (int i = 0; i < size; i++) {
                            arrayKey = key + "";
                            parseHashMapTwoKey(jsonArray.getJSONObject(i));
                        }
                    } else if (data.get(key) instanceof JSONObject) {
                        objectKey = key + "";
                        parseHashMapTwoKey(data.getJSONObject(key));
                    } else {
                        hashMapSecond.put(key, data.getString(key));
                        hMapTwoKey.put(arrayKey, hashMapSecond);
                    }
                } catch (Throwable e) {
                    try {
                    } catch (Exception ee) {
                    }
                    e.printStackTrace();
                }
            }
        }
    }

    //    json array parsing
    public ArrayList<String> jsonEmptyArrayListParse(JSONArray jsonArray) {
        ArrayList<String> alData = new ArrayList<String>();
        try {
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    alData.add(jsonArray.get(i) + "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return alData;
    }

    //    json array parsing
    public ArrayList<String> jsonKeyArrayListParse(JSONArray jsonArray, String key) {
        ArrayList<String> alData = new ArrayList<String>();
        try {
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String strJsonValue = jsonObject.getString(key);
                    alData.add(strJsonValue + "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return alData;
    }

    //
//    json array get set
    public ArrayList<ApiDataModel> jsonKeyArrayGetSet(JSONArray jsonArray, String key, String keyTwo) {
        ArrayList<ApiDataModel> alData = new ArrayList<ApiDataModel>();
        try {
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String strJsonValue = jsonObject.getString(key);
                    String strJsonValueName = jsonObject.getString(keyTwo);
                    ApiDataModel apiDataModel = new ApiDataModel(strJsonValue + "", strJsonValueName + "", strJsonValueName);
                    alData.add(apiDataModel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return alData;
    }

    public HashMap<String, String> jsonHashMapParse(JSONArray jsonArray) {
        HashMap<String, String> jsonHashMap = new HashMap<>();
        try {
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonHashMap.put("contacts", jsonArray.get(i) + "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonHashMap;
    }

    //    json array parsing
    public ArrayList<HashMap<String, String>> jsonEmptyHashMapParse(JSONArray jsonArray) {
        ArrayList<HashMap<String, String>> contactList = new ArrayList<>();
        try {
            if (jsonArray != null) {
                JSONObject jsonObject;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    //                    String id = jsonArray.getJSONObject(i).getString("id") + "";
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("name");
                    String email = jsonObject.getString("email");
                    HashMap<String, String> hashMapValues = new HashMap<>();
                    hashMapValues.put("id", id);
                    hashMapValues.put("name", name);
                    hashMapValues.put("email", email);
                    contactList.add(hashMapValues);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contactList;
    }

    //    json array keys parsing
    public ArrayList<HashMap<String, String>> jsonHashMapArrayKeys(JSONArray jsonArray, String[] keys) {
        ArrayList<HashMap<String, String>> contactList = new ArrayList<>();
        try {
            if (jsonArray != null) {
                JSONObject jsonObject;
                Log.e("jsonArray",jsonArray.length()+"");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMapValues= new HashMap<>();;
                    for (int j = 0; j < keys.length; j++) {
                        String valueResponse = jsonObject.getString(keys[j]);
                        Log.e("valueResponse",valueResponse+"");
                        hashMapValues.put(keys[j], valueResponse);
                    }
                    contactList.add(hashMapValues);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("contactList",contactList.size()+"");
        return contactList;
    }
}
