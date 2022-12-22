package com.karl.pfind.ui.services;

public class Result {

    private String resultMessage;
    private Object result;

    public Result() {}

    public Result(String resultMessage, Object result) {
        this.resultMessage = resultMessage;
        this.result = result;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
