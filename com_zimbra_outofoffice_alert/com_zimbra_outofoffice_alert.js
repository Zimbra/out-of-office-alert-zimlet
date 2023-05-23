function com_zimbra_outofoffice_alert_HandlerObject() {
   com_zimbra_outofoffice_alert_HandlerObject.settings = {};
};

com_zimbra_outofoffice_alert_HandlerObject.prototype = new ZmZimletBase();
com_zimbra_outofoffice_alert_HandlerObject.prototype.constructor = com_zimbra_outofoffice_alert_HandlerObject;
var outOfOfficeAlertZimlet = com_zimbra_outofoffice_alert_HandlerObject;

outOfOfficeAlertZimlet.prototype.init = function () {
   AjxDispatcher.require(["MailCore"]);
   try {
      ZmRecipients.prototype._bubblesChangedCallback =
      function() {
         if (this._resetContainerSize) {
         this._resetContainerSize(); // body size might change due to change in size of address field (due to new bubbles).
        }
         outOfOfficeAlertZimlet.prototype.bubblesChanged(this);
      };
   } catch (e) {console.log(e);}
};

/* status method show a Zimbra status message
 * */
outOfOfficeAlertZimlet.prototype.status = function(text, type) {
   var transitions = [ ZmToast.FADE_IN, ZmToast.PAUSE, ZmToast.FADE_OUT ];
   appCtxt.getAppController().setStatusMsg(text, type, null, transitions);
}; 

outOfOfficeAlertZimlet.prototype.bubblesChanged = function(ZmRecipients) {
   var zimletInstance = appCtxt._zimletMgr.getZimletByName('com_zimbra_outofoffice_alert').handlerObject;
   var rawAddresses = ZmRecipients.getRawAddrFields();
   var allAddresses = rawAddresses.TO + " " + rawAddresses.CC;
   if(rawAddresses.BCC)
   {
      allAddresses = allAddresses + " " + rawAddresses.BCC;
   }
   allAddresses = allAddresses.replaceAll('<', ' ');
   var regex = /\S+[a-z0-9]@[a-z0-9\.]+/img;
   var recipients = allAddresses.match(regex);

   var requestData = {}
   requestData.accounts = recipients;
   var request = new XMLHttpRequest();
   var url = '/service/extension/outofofficebanner';
   var formData = new FormData();
   formData.append("jsondata", JSON.stringify(requestData));
   request.open('POST', url);
   request.onreadystatechange = function (e) {
      if (request.readyState == 4) {
         if (request.status == 200) {
            var response = JSON.parse(request.responseText);
            var hasOoo = false;
            var recipientsWithOoo = "";
            for (var key in response) {
               if (response.hasOwnProperty(key)) {
                  if(response[key] == true)
                  {
                     hasOoo = true;
                     recipientsWithOoo = recipientsWithOoo + key;
                  }
               }
            }
            if(hasOoo)
            {
               var zimletInstance = appCtxt._zimletMgr.getZimletByName('com_zimbra_outofoffice_alert').handlerObject;
               zimletInstance.status(zimletInstance.getMessage('outOfOfficeAlertZimlet_message') + " " + recipientsWithOoo);
            }
         }
      }
   }
   request.send(formData);
};

/* status method show a Zimbra status message
 * */
outOfOfficeAlertZimlet.prototype.status = function(text, type) {
   var transitions = [ ZmToast.FADE_IN, ZmToast.PAUSE, ZmToast.PAUSE, ZmToast.PAUSE, ZmToast.FADE_OUT ];
   appCtxt.getAppController().setStatusMsg(text, type, null, transitions);
}; 

