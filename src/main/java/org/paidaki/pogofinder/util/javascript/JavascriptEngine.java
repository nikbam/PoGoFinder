package org.paidaki.pogofinder.util.javascript;

import netscape.javascript.JSObject;
import org.paidaki.pogofinder.util.threading.ThreadManager;
import org.paidaki.pogofinder.web.Browser;

public class JavascriptEngine {

    private Browser browser;

    public JavascriptEngine(Browser browser) {
        this.browser = browser;
    }

    private String jsFunctionString(String function, Object... parameters) {
        StringBuilder command = new StringBuilder();
        String prefix = "";

        command.append(function).append("( ");

        for (Object p : parameters) {
            command.append(prefix)
                    .append(String.valueOf(p));
            prefix = ", ";
        }
        command.append(" );");

        return command.toString();
    }

    public Object jsStringFunctionFX(String function, Object... parameters) {
        String command = jsFunctionString(function, parameters);

        return ThreadManager.runAndWaitOnFXThread(() -> browser.webEngine.executeScript(command));
    }

    public Object jsObjectFunctionFX(String function, Object... parameters) {
        return ThreadManager.runAndWaitOnFXThread(() -> browser.jsWindow.call(function, parameters));
    }

    public Object jsObjectFunctionFX(JSObject callee, String function, Object... parameters) {
        return ThreadManager.runAndWaitOnFXThread(() -> callee.call(function, parameters));
    }

    public Object jsStringFunction(String function, Object... parameters) {
        String command = jsFunctionString(function, parameters);

        return ThreadManager.runAndWaitOnFXThread(() -> browser.webEngine.executeScript(command));
    }

    public Object jsObjectFunction(String function, Object... parameters) {
        return ThreadManager.runAndWaitOnFXThread(() -> browser.jsWindow.call(function, parameters));
    }

    public Object jsObjectFunction(JSObject callee, String function, Object... parameters) {
        return ThreadManager.runAndWaitOnFXThread(() -> callee.call(function, parameters));
    }

    public Object jsStringSetTimeoutFX(long timeout, String function, Object... parameters) {
        String command = jsFunctionString(function, parameters);

        return ThreadManager.runAndWaitOnFXThread(() -> browser.webEngine.executeScript(
                "setTimeout( function() { " + command + " }, "
                        + timeout + " );"));
    }

    public Object jsObjectSetTimeoutFX(Object function, long timeout) {
        return ThreadManager.runAndWaitOnFXThread(() -> browser.jsWindow.call("setTimeout", function, timeout));
    }

    public Object jsStringSetTimeout(long timeout, String function, Object... parameters) {
        String command = jsFunctionString(function, parameters);

        return ThreadManager.runAndWaitOnFXThread(() -> browser.webEngine.executeScript(
                "setTimeout( function() { " + command + " }, "
                        + timeout + " );"));
    }

    public Object jsObjectSetTimeout(Object function, long timeout) {
        return ThreadManager.runAndWaitOnFXThread(() -> browser.jsWindow.call("setTimeout", function, timeout));
    }

    public Object jsScript(String script) {
        return ThreadManager.runAndWaitOnFXThread(() -> browser.webEngine.executeScript(script));
    }

    public Object getJsObject(String name) {
        return jsScript(name);
    }

    public void setJsObject(String name, Object value) {
        ThreadManager.runOnFXThread(() -> browser.jsWindow.setMember(name, value));
    }

    public Object getJsMember(JSObject object, String member) {
        return ThreadManager.runAndWaitOnFXThread(() -> object.getMember(member));
    }

    public void setJsMember(JSObject object, String member, Object value) {
        ThreadManager.runOnFXThread(() -> object.setMember(member, value));
    }
}
