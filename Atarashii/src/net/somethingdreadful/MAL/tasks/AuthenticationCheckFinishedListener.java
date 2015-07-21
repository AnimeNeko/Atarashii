package net.somethingdreadful.MAL.tasks;

public interface AuthenticationCheckFinishedListener {
    public void onAuthenticationCheckFinished(boolean result, String username);
}
