package org.wordpress.android.fluxc.store;

import android.support.annotation.NonNull;

import com.android.volley.VolleyError;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.fluxc.Dispatcher;
import org.wordpress.android.fluxc.Payload;
import org.wordpress.android.fluxc.action.AccountAction;
import org.wordpress.android.fluxc.action.AuthenticationAction;
import org.wordpress.android.fluxc.annotations.action.Action;
import org.wordpress.android.fluxc.annotations.action.IAction;
import org.wordpress.android.fluxc.model.AccountModel;
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder;
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryError;
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryResultPayload;
import org.wordpress.android.fluxc.network.rest.wpcom.account.AccountRestClient;
import org.wordpress.android.fluxc.network.rest.wpcom.account.AccountRestClient.AccountPushSettingsResponsePayload;
import org.wordpress.android.fluxc.network.rest.wpcom.account.AccountRestClient.AccountRestPayload;
import org.wordpress.android.fluxc.network.rest.wpcom.account.AccountRestClient.IsAvailable;
import org.wordpress.android.fluxc.network.rest.wpcom.account.AccountRestClient.IsAvailableResponsePayload;
import org.wordpress.android.fluxc.network.rest.wpcom.account.AccountRestClient.NewAccountResponsePayload;
import org.wordpress.android.fluxc.network.rest.wpcom.auth.AccessToken;
import org.wordpress.android.fluxc.network.rest.wpcom.auth.Authenticator;
import org.wordpress.android.fluxc.network.rest.wpcom.auth.Authenticator.AuthenticateErrorPayload;
import org.wordpress.android.fluxc.network.rest.wpcom.auth.Authenticator.Token;
import org.wordpress.android.fluxc.persistence.AccountSqlUtils;
import org.wordpress.android.fluxc.store.SiteStore.RefreshSitesXMLRPCPayload;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * In-memory based and persisted in SQLite.
 */
@Singleton
public class AccountStore extends Store {
    // Payloads
    public static class AuthenticatePayload extends Payload {
        public String username;
        public String password;
        public String twoStepCode;
        public boolean shouldSendTwoStepSms;
        public Action nextAction;
        public AuthenticatePayload(@NonNull String username, @NonNull String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class PushAccountSettingsPayload extends Payload {
        public Map<String, Object> params;
        public PushAccountSettingsPayload() {
        }
    }

    public static class NewAccountPayload extends Payload {
        public String username;
        public String password;
        public String email;
        public boolean dryRun;
        public NewAccountPayload(@NonNull String username, @NonNull String password, @NonNull String email,
                                 boolean dryRun) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.dryRun = dryRun;
        }
    }

    public static class UpdateTokenPayload extends Payload {
        public UpdateTokenPayload(String token) {
            this.token = token;
        }

        public String token;
    }

    // OnChanged Events
    public class OnAccountChanged extends OnChanged<AccountError> {
        public boolean accountInfosChanged;
        public AccountAction causeOfChange;
    }

    public class OnAuthenticationChanged extends OnChanged<AuthenticationError> {
    }

    public class OnDiscoveryResponse extends OnChanged<DiscoveryError> {
        public String xmlRpcEndpoint;
        public String wpRestEndpoint;
        public String failedEndpoint;
    }

    public class OnNewUserCreated extends OnChanged<NewUserError> {
        public boolean dryRun;
    }

    public class OnAvailabilityChecked extends OnChanged<IsAvailableError> {
        public IsAvailable type;
        public String value;
        public boolean isAvailable;
        public List<String> suggestions;

        public OnAvailabilityChecked(IsAvailable type, String value, boolean isAvailable) {
            this.type = type;
            this.value = value;
            this.isAvailable = isAvailable;
        }
    }

    public static class AuthenticationError implements OnChangedError {
        public AuthenticationErrorType type;
        public String message;
        public AuthenticationError(AuthenticationErrorType type, @NonNull String message) {
            this.type = type;
            this.message = message;
        }
    }

    // Enums
    public enum AuthenticationErrorType {
        // From response's "error" field
        ACCESS_DENIED,
        AUTHORIZATION_REQUIRED,
        INVALID_CLIENT,
        INVALID_GRANT,
        INVALID_OTP,
        INVALID_REQUEST,
        INVALID_TOKEN,
        NEEDS_2FA,
        UNSUPPORTED_GRANT_TYPE,
        UNSUPPORTED_RESPONSE_TYPE,
        UNKNOWN_TOKEN,

        // From response's "message" field - sadly... (be careful with i18n)
        INCORRECT_USERNAME_OR_PASSWORD,

        // .org specifics
        INVALID_SSL_CERTIFICATE,
        HTTP_AUTH_ERROR,
        NOT_AUTHENTICATED,

        // Generic error
        GENERIC_ERROR;

        public static AuthenticationErrorType fromString(String string) {
            if (string != null) {
                for (AuthenticationErrorType v : AuthenticationErrorType.values()) {
                    if (string.equalsIgnoreCase(v.name())) {
                        return v;
                    }
                }
            }
            return GENERIC_ERROR;
        }
    }

    public static class AccountError implements OnChangedError {
        public AccountErrorType type;
        public String message;
        public AccountError(AccountErrorType type, @NonNull String message) {
            this.type = type;
            this.message = message;
        }
    }

    public enum AccountErrorType {
        ACCOUNT_FETCH_ERROR,
        SETTINGS_FETCH_ERROR,
        SETTINGS_POST_ERROR,
        GENERIC_ERROR
    }

    public static class IsAvailableError implements OnChangedError {
        public IsAvailableErrorType type;
        public String message;

        public IsAvailableError(@NonNull String type, @NonNull String message) {
            this.type = IsAvailableErrorType.fromString(type);
            this.message = message;
        }
    }

    public enum IsAvailableErrorType {
        INVALID,
        GENERIC_ERROR;

        public static IsAvailableErrorType fromString(String string) {
            if (string != null) {
                for (IsAvailableErrorType v : IsAvailableErrorType.values()) {
                    if (string.equalsIgnoreCase(v.name())) {
                        return v;
                    }
                }
            }
            return GENERIC_ERROR;
        }
    }

    public static class NewUserError implements OnChangedError {
        public NewUserErrorType type;
        public String message;
        public NewUserError(NewUserErrorType type, @NonNull String message) {
            this.type = type;
            this.message = message;
        }
    }

    public enum NewUserErrorType {
        USERNAME_ONLY_LOWERCASE_LETTERS_AND_NUMBERS,
        USERNAME_REQUIRED,
        USERNAME_NOT_ALLOWED,
        USERNAME_MUST_BE_AT_LEAST_FOUR_CHARACTERS,
        USERNAME_CONTAINS_INVALID_CHARACTERS,
        USERNAME_MUST_INCLUDE_LETTERS,
        USERNAME_EXISTS,
        USERNAME_RESERVED_BUT_MAY_BE_AVAILABLE,
        USERNAME_INVALID,
        PASSWORD_INVALID,
        EMAIL_CANT_BE_USED_TO_SIGNUP,
        EMAIL_INVALID,
        EMAIL_NOT_ALLOWED,
        EMAIL_EXISTS,
        EMAIL_RESERVED,
        GENERIC_ERROR;

        public static NewUserErrorType fromString(String string) {
            if (string != null) {
                for (NewUserErrorType v : NewUserErrorType.values()) {
                    if (string.equalsIgnoreCase(v.name())) {
                        return v;
                    }
                }
            }
            return GENERIC_ERROR;
        }
    }

    // Fields
    private AccountRestClient mAccountRestClient;
    private Authenticator mAuthenticator;
    private AccountModel mAccount;
    private AccessToken mAccessToken;
    private SelfHostedEndpointFinder mSelfHostedEndpointFinder;

    @Inject
    public AccountStore(Dispatcher dispatcher, AccountRestClient accountRestClient,
                        SelfHostedEndpointFinder selfHostedEndpointFinder, Authenticator authenticator,
                        AccessToken accessToken) {
        super(dispatcher);
        mAuthenticator = authenticator;
        mAccountRestClient = accountRestClient;
        mSelfHostedEndpointFinder = selfHostedEndpointFinder;
        mAccount = loadAccount();
        mAccessToken = accessToken;
    }

    @Override
    public void onRegister() {
        AppLog.d(T.API, "AccountStore onRegister");
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    @Override
    public void onAction(Action action) {
        IAction actionType = action.getType();
        if (actionType instanceof AccountAction) {
            onAccountAction((AccountAction) actionType, action.getPayload());
        }
        if (actionType instanceof AuthenticationAction) {
            onAuthenticationAction((AuthenticationAction) actionType, action.getPayload());
        }
    }

    private void onAccountAction(AccountAction actionType, Object payload) {
        switch (actionType) {
            case FETCH_ACCOUNT:
                mAccountRestClient.fetchAccount();
                break;
            case FETCH_SETTINGS:
                mAccountRestClient.fetchAccountSettings();
                break;
            case PUSH_SETTINGS:
                mAccountRestClient.pushAccountSettings(((PushAccountSettingsPayload) payload).params);
                break;
            case UPDATE_ACCOUNT:
                updateDefaultAccount((AccountModel) payload, AccountAction.UPDATE_ACCOUNT);
                break;
            case UPDATE_ACCESS_TOKEN:
                updateToken((UpdateTokenPayload) payload);
                break;
            case SIGN_OUT:
                signOut();
                break;
            case CREATE_NEW_ACCOUNT:
                createNewAccount((NewAccountPayload) payload);
                break;
            case CREATED_NEW_ACCOUNT:
                handleNewAccountCreated((NewAccountResponsePayload) payload);
                break;
            case PUSHED_SETTINGS:
                handlePushSettingsCompleted((AccountPushSettingsResponsePayload) payload);
                break;
            case FETCHED_SETTINGS:
                handleFetchSettingsCompleted((AccountRestPayload) payload);
                break;
            case FETCHED_ACCOUNT:
                handleFetchAccountCompleted((AccountRestPayload) payload);
                break;
            case IS_AVAILABLE_BLOG:
                mAccountRestClient.isAvailable((String) payload, IsAvailable.BLOG);
                break;
            case IS_AVAILABLE_DOMAIN:
                mAccountRestClient.isAvailable((String) payload, IsAvailable.DOMAIN);
                break;
            case IS_AVAILABLE_EMAIL:
                mAccountRestClient.isAvailable((String) payload, IsAvailable.EMAIL);
                break;
            case IS_AVAILABLE_USERNAME:
                mAccountRestClient.isAvailable((String) payload, IsAvailable.USERNAME);
                break;
            case CHECKED_IS_AVAILABLE:
                handleCheckedIsAvailable((IsAvailableResponsePayload) payload);
                break;
        }
    }

    private void onAuthenticationAction(AuthenticationAction actionType, Object payload) {
        switch (actionType) {
            case AUTHENTICATE:
                authenticate((AuthenticatePayload) payload);
                break;
            case AUTHENTICATE_ERROR:
                handleAuthenticateError((AuthenticateErrorPayload) payload);
                break;
            case DISCOVER_ENDPOINT:
                discoverEndPoint((RefreshSitesXMLRPCPayload) payload);
                break;
            case DISCOVERY_RESULT:
                discoveryResult((DiscoveryResultPayload) payload);
                break;
        }
    }

    private void handleAuthenticateError(AuthenticateErrorPayload payload) {
        OnAuthenticationChanged event = new OnAuthenticationChanged();
        event.error = payload.error;
        emitChange(event);
    }
    private void discoverEndPoint(RefreshSitesXMLRPCPayload payload) {
        mSelfHostedEndpointFinder.findEndpoint(payload.url, payload.username, payload.password);
    }

    private void discoveryResult(DiscoveryResultPayload payload) {
        OnDiscoveryResponse discoveryResponse = new OnDiscoveryResponse();
        if (payload.isError()) {
            discoveryResponse.error = DiscoveryError.GENERIC_ERROR;
            discoveryResponse.failedEndpoint = payload.failedEndpoint;
        } else if (payload.isDiscoveryError()) {
            discoveryResponse.error = payload.discoveryError;
            discoveryResponse.failedEndpoint = payload.failedEndpoint;
        } else {
            discoveryResponse.xmlRpcEndpoint = payload.xmlRpcEndpoint;
            discoveryResponse.wpRestEndpoint = payload.wpRestEndpoint;
        }
        emitChange(discoveryResponse);
    }

    private void handleFetchAccountCompleted(AccountRestPayload payload) {
        if (!checkError(payload, "Error fetching Account via REST API (/me)")) {
            mAccount.copyAccountAttributes(payload.account);
            updateDefaultAccount(mAccount, AccountAction.FETCH_ACCOUNT);
        } else {
            emitAccountChangeError(AccountErrorType.ACCOUNT_FETCH_ERROR);
        }
    }

    private void handleFetchSettingsCompleted(AccountRestPayload payload) {
        if (!checkError(payload, "Error fetching Account Settings via REST API (/me/settings)")) {
            mAccount.copyAccountSettingsAttributes(payload.account);
            updateDefaultAccount(mAccount, AccountAction.FETCH_SETTINGS);
        } else {
            emitAccountChangeError(AccountErrorType.SETTINGS_FETCH_ERROR);
        }
    }

    private void handlePushSettingsCompleted(AccountPushSettingsResponsePayload payload) {
        if (!payload.isError()) {
            boolean updated = AccountRestClient.updateAccountModelFromPushSettingsResponse(mAccount, payload.settings);
            if (updated) {
                updateDefaultAccount(mAccount, AccountAction.PUSH_SETTINGS);
            } else {
                OnAccountChanged accountChanged = new OnAccountChanged();
                accountChanged.causeOfChange = AccountAction.PUSH_SETTINGS;
                accountChanged.accountInfosChanged = false;
                emitChange(accountChanged);
            }
        } else {
            emitAccountChangeError(AccountErrorType.SETTINGS_POST_ERROR);
        }
    }

    private void handleNewAccountCreated(NewAccountResponsePayload payload) {
        OnNewUserCreated onNewUserCreated = new OnNewUserCreated();
        onNewUserCreated.error = payload.error;
        onNewUserCreated.dryRun = payload.dryRun;
        emitChange(onNewUserCreated);
    }

    private void handleCheckedIsAvailable(IsAvailableResponsePayload payload) {
        OnAvailabilityChecked event = new OnAvailabilityChecked(payload.type, payload.value, payload.isAvailable);
        event.suggestions = payload.suggestions;

        if (payload.isError()) {
            event.error = payload.error;
        }

        emitChange(event);
    }

    private void emitAccountChangeError(AccountErrorType errorType) {
        OnAccountChanged event = new OnAccountChanged();
        event.error = new AccountError(errorType, null);
        emitChange(event);
    }

    private void createNewAccount(NewAccountPayload payload) {
        mAccountRestClient.newAccount(payload.username, payload.password, payload.email, payload.dryRun);
    }

    private void signOut() {
        // Remove Account
        AccountSqlUtils.deleteAccount(mAccount);
        mAccount.init();
        OnAccountChanged accountChanged = new OnAccountChanged();
        accountChanged.accountInfosChanged = true;
        emitChange(accountChanged);
        // Remove authentication token
        mAccessToken.set(null);
        emitChange(new OnAuthenticationChanged());
    }

    public AccountModel getAccount() {
        return mAccount;
    }

    /**
     * Can be used to check if Account is signed into WordPress.com.
     */
    public boolean hasAccessToken() {
        return mAccessToken.exists();
    }

    /**
     * Should be used for very specific purpose (like forwarding the token to a Webview)
     */
    public String getAccessToken() {
        return mAccessToken.get();
    }

    private void updateToken(UpdateTokenPayload updateTokenPayload) {
        mAccessToken.set(updateTokenPayload.token);
    }

    private void updateDefaultAccount(AccountModel accountModel, AccountAction cause) {
        // Update memory instance
        mAccount = accountModel;
        AccountSqlUtils.insertOrUpdateDefaultAccount(accountModel);
        OnAccountChanged accountChanged = new OnAccountChanged();
        accountChanged.accountInfosChanged = true;
        accountChanged.causeOfChange = cause;
        emitChange(accountChanged);
    }

    private AccountModel loadAccount() {
        AccountModel account = AccountSqlUtils.getDefaultAccount();
        return account == null ? new AccountModel() : account;
    }

    private void authenticate(final AuthenticatePayload payload) {
        mAuthenticator.authenticate(payload.username, payload.password, payload.twoStepCode,
                payload.shouldSendTwoStepSms, new Authenticator.Listener() {
                    @Override
                    public void onResponse(Token token) {
                        mAccessToken.set(token.getAccessToken());
                        if (payload.nextAction != null) {
                            mDispatcher.dispatch(payload.nextAction);
                        }
                        emitChange(new OnAuthenticationChanged());
                    }
                }, new Authenticator.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        AppLog.e(T.API, "Authentication error");
                        OnAuthenticationChanged event = new OnAuthenticationChanged();
                        event.error = new AuthenticationError(
                                Authenticator.volleyErrorToAuthenticationError(volleyError),
                                Authenticator.volleyErrorToErrorMessage(volleyError));
                        emitChange(event);
                    }
                });
    }

    private boolean checkError(AccountRestPayload payload, String log) {
        if (payload.isError()) {
            AppLog.w(T.API, log + "\nError: " + payload.error.volleyError);
            return true;
        }
        return false;
    }
}
