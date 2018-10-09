package com.project.dayworx.network;

/**
 * Created by ubuntu on 10/8/16.
 */

public interface IWebService {

//    String MAIN_URL = "http://dddemo.net/php/fish_factory/index.php/";

    String MAIN_URL = "http://dayworxapp.com/api/settings.php";

    String DEVICE_TYPE = "android";

    String KEY_RES_DATA = "data";
    String KEY_RES_SUCCESS = "success";
    String KEY_RES_MESSAGE = "message";

    String KEY_ACTION_SIGNUP = "register";
    String KEY_ACTION_LOGIN = "login";
    String KEY_ACTION_UPDATE_PROFILE = "update";
    String KEY_ACTION_CREATE_REPORT = "create_report";
    String KEY_ACTION_SAVE_IMAGE = "save_report_image";
    String KEY_ACTION_VERIFY_REPORT = "verify_report";
    String KEY_ACTION_REPORT_HISTORY = "dayworx_history";
    String KEY_ACTION_FORGET_PASSWORD = "forgot_password";
    String KEY_ACTION_REPORT_DRAFT = "dayworx_report_draft";
    String KEY_ACTION_REPORT_UPDATE = "update_report";
    String KEY_ACTION_REPORT_PURCHASE = "report_purchased";
    String KEY_ACTION_CHANGE_PASSWORD = "change_password";
    String KEY_ACTION_SEND_FEEDBACK = "feedback";
    String KEY_ACTION_DELETE_REPORT = "report_delete";


    String KEY_USER_ID = "user_id";

    String KEY_CONSTANT_DRAFT = "draft";


    /**
     * Request Params
     */
    String KEY_REQ_ACTION = "action";

    String KEY_REQ_DEVICE_ID = "device_id";
    String KEY_REQ_NAME = "name";
    String KEY_REQ_EMAIL = "email";
    String KEY_REQ_MOBILE = "mobile";
    String KEY_REQ_PASSWORD = "password";
    String KEY_REQ_PROFILE_PIC = "profile_image";
    String KEY_REQ_PAGE_NO = "page_no";
    String KEY_REQ_SITE_ADDRESS = "site_address";
    String KEY_REQ_PROJECT_NAME = "project_name";
    String KEY_REQ_TRADE = "trade";
    String KEY_REQ_ORDER_NUMBER = "order_no";
    String KEY_REQ_WORK_REQUIRED = "work_required";
    String KEY_REQ_WORK_SPECIFIC_AREA = "work_specific_area";
    String KEY_REQ_REPAIRING_DAMAGE = "repairing_damage";
    String KEY_REQ_MATERIAL_USED = "material_used";
    String KEY_REQ_WORKING_HOURS = "working_hours";
    String KEY_REQ_TOTAL_HOURS = "total_hours";
    String KEY_REQ_DAY = "day";
    String KEY_REQ_TIME = "time";
    String KEY_REQ_TOTAL_MINUTES = "total_minutes";
    String KEY_REQ_IMAGE_ARRAY = "image_list";
    String KEY_REQ_VERIFIER_IMAGE = "image";
    String KEY_REQ_VERIFIER_NAME = "verifier_name";
    String KEY_REQ_TITLE = "title";
    String KEY_REQ_COMPANY = "company";
    String KEY_REQ_VERIFICATION_DATE = "date";
    String KEY_REQ_VERIFICATION_COMMENTS = "comments";
    String KEY_REQ_REPORT_ID = "report_id";
    String KEY_REQ_EMAIL_ID = "email_id";
    String KEY_REQ_OLD_PASSWORD = "old_password";
    String KEY_REQ_NEW_PASSWORD = "new_password";
    String KEY_REQ_FEEDBACK = "feedback_text";


    /**
     * Response Params
     */

    String KEY_RES_USER_ID = "user_id";
    String KEY_RES_PROFILE_PIC = "profile_image";
    String KEY_RES_REPORT_ID = "report_id";
    String KEY_RES_REPORT_HISTORY = "report_document";
    String KEY_RES_REPORT_PURCHASED_STATUS = "purchased_status";

}
