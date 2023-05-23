package com.zimbra.outofofficebanner;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.extension.ExtensionHttpHandler;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.servlet.util.AuthUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class OutOfOfficeBanner extends ExtensionHttpHandler {

    /**
     * @return path
     */
    @Override
    public String getPath() {
        return "/outofofficebanner";
    }

    /**
     * Processes HTTP GET requests.
     *
     * @param req  request message
     * @param resp response message
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getOutputStream().print("com.zimbra.outofofficebanner is installed.");
    }

    /**
     * Processes HTTP POST requests.
     *
     * @param req  request message
     * @param resp response message
     */

    /* This extension works with aliasses as well. */
    /*
      let testData = {}
      testData.accounts = ["info@barrydegraaff.nl", "admin@barrydegraaff.nl"];
      var request = new XMLHttpRequest();
      var url = '/service/extension/outofofficebanner';
      var formData = new FormData();
      formData.append("jsondata", JSON.stringify(testData));
      request.open('POST', url);
      request.onreadystatechange = function (e) {
         if (request.readyState == 4) {
            if (request.status == 200) {
               const Response = JSON.parse(request.responseText);
               console.log(Response);
            }
            if (request.status == 400) {
               const Response = JSON.parse(request.responseText);
               console.log(Response);
            }
         }
      }
      request.send(formData);
    * */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //all authentication is done by AuthUtil.getAuthTokenFromHttpReq, returns null if unauthorized
        final AuthToken authToken = AuthUtil.getAuthTokenFromHttpReq(req, resp, false, true);
        if (authToken != null) {
            try {
                //Get the jsondata field from the multipart request send to the server and parse it to JSON Object.
                JSONObject receivedJSON = new JSONObject(IOUtils.toString(req.getPart("jsondata").getInputStream(), StandardCharsets.UTF_8));
                JSONArray reqAccounts = receivedJSON.getJSONArray("accounts");
                JSONObject responseJSON = new JSONObject();

                //loop to get all json objects from data json array
                for (int i = 0; i < reqAccounts.length(); i++) {
                    //use of toLowerCase to prevent people from using this endpoint to determine if accounts exist on server (privacy).
                    String reqAccount = reqAccounts.get(i).toString();

                    boolean replyInEffect = false;
                    try {
                        Account account = Provisioning.getInstance().getAccountByName(reqAccount);
                        boolean replyEnabled = account.isPrefOutOfOfficeReplyEnabled();

                        // Check if we are in any configured out of office interval
                        Date now = new Date();
                        Date fromDate = account.getGeneralizedTimeAttr(Provisioning.A_zimbraPrefOutOfOfficeFromDate, null);
                        Date untilDate = account.getGeneralizedTimeAttr(Provisioning.A_zimbraPrefOutOfOfficeUntilDate, null);

                        if (replyEnabled && (fromDate != null && !now.before(fromDate))) {
                            if (untilDate == null) {
                                replyInEffect = true;
                            } else if (untilDate != null && !now.after(untilDate)) {
                                replyInEffect = true;
                            } else {
                                replyInEffect = false;
                            }
                        }
                        responseJSON.put(reqAccount, replyInEffect);

                    } catch (Exception e) {
                        responseJSON.put(reqAccount, false);
                    }

                }
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getOutputStream().print(responseJSON.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
