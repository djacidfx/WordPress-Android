package org.wordpress.android.fluxc.network.rest;

import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.wordpress.android.fluxc.network.BaseRequest;
import org.wordpress.android.fluxc.network.rest.wpcom.account.AccountSocialResponse;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

import java.util.Map;

public class FormRequest extends BaseRequest<AccountSocialResponse> {
    private static final String PROTOCOL_CHARSET = "utf-8";
    private static final String PROTOCOL_CONTENT = "application/x-www-form-urlencoded";

    private final Map<String, String> mParams;
    private final Response.Listener<AccountSocialResponse> mListener;

    public FormRequest(String url, Map<String, String> params, Response.Listener<AccountSocialResponse> listener,
                       BaseErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        mParams = params;
        mListener = listener;
    }

    @Override
    public BaseNetworkError deliverBaseNetworkError(@NonNull BaseNetworkError error) {
        return error;
    }

    @Override
    protected void deliverResponse(AccountSocialResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public String getBodyContentType() {
        return String.format("%s; charset=%s", PROTOCOL_CONTENT, PROTOCOL_CHARSET);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    @Override
    protected Response<AccountSocialResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            AccountSocialResponse parsed = new AccountSocialResponse();
            String responseBody = new String(response.data);
            JSONObject object = new JSONObject(responseBody);
            JSONObject data = object.getJSONObject("data");
            parsed.bearer_token = data.optString("bearer_token");
            return Response.success(parsed, null);
        } catch (JSONException exception) {
            AppLog.e(T.API, "Unable to parse network response: " + exception.getMessage());
            return null;
        }
    }
}
