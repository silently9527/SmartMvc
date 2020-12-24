package com.silently9527.smartmvc.handler;

import com.silently9527.smartmvc.http.HttpStatus;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Objects;

public class ModelAndViewContainer {
    private Object view;
    private Model model;
    private HttpStatus status;
    private boolean requestHandled = false;

    public void setView(Object view) {
        this.view = view;
    }

    public String getViewName() {
        return (this.view instanceof String ? (String) this.view : null);
    }

    public void setViewName(String viewName) {
        this.view = viewName;
    }

    public Object getView() {
        return this.view;
    }

    public boolean isViewReference() {
        return (this.view instanceof String);
    }

    public Model getModel() {
        if (Objects.isNull(this.model)) {
            this.model = new ExtendedModelMap();
        }
        return this.model;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    public boolean isRequestHandled() {
        return requestHandled;
    }

    public void setRequestHandled(boolean requestHandled) {
        this.requestHandled = requestHandled;
    }
}
