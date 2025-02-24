package com.epam.deltix.qsrv.hf.tickdb.web.controller;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.qsrv.hf.tickdb.http.AbstractHandler;
import com.epam.deltix.qsrv.hf.tickdb.pub.mon.TBMonitor;
import com.epam.deltix.qsrv.hf.tickdb.web.model.impl.ModelFactoryImpl;
import com.epam.deltix.qsrv.hf.tickdb.web.model.pub.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MonitorServlet extends HttpServlet {
    protected static final Log LOG = LogFactory.getLog(MonitorServlet.class);
    public static final String MODEL_ARG = "model";
    public static final String ALERT_TYPE_ARG = "alert_type";
    public static final String ALERT_TYPE_DANGER = "danger";
    public static final String ALERT_MSG_ARG = "alert_msg";

    private final ModelFactory modelFactory;

    public MonitorServlet() {
        modelFactory = ModelFactoryImpl.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            if (path.equals("/") || path.equals("/index")) {
                resp.sendRedirect(req.getContextPath() + "/cursors");
            } else if (path.startsWith("/loaders")) {
                if (path.equals("/loaders")) {
                    handleLoaders(req, resp);
                } else {
                    handleLoader(req, resp);
                }
            } else if (path.startsWith("/cursors")) {
                if (path.equals("/cursors")) {
                    handleCursors(req, resp);
                } else {
                    handleCursor(req, resp);
                }
            } else if (path.equals("/connections")) {
                handleConnections(req, resp);
            } else if (path.equals("/locks")) {
                handleLocks(req, resp);
            } else if (path.startsWith("/track")) {
                handleTrack(req, resp);
            } else {
                handleNotFound(resp);
            }
        } catch (Exception e) {
            LOG.error("Error processing request: %s").with(e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void handleLoaders(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        LoadersModel loadersModel = modelFactory.getLoadersModel();
        req.setAttribute(MODEL_ARG, loadersModel);
        req.getRequestDispatcher("/WEB-INF/jsp/loaders.jsp").forward(req, resp);
    }

    private void handleLoader(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String idParam = req.getPathInfo().substring(1);
        Long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            redirectWithMessage(req, resp, "/loaders", ALERT_TYPE_DANGER, "Invalid loader id format.");
            return;
        }

        LoaderModel loaderModel = modelFactory.getLoaderModel(id);
        if (loaderModel.getLoader() == null) {
            redirectWithMessage(req, resp, "/loaders", ALERT_TYPE_DANGER, "Unknown loader with id " + id + ".");

        } else {
            req.setAttribute(MODEL_ARG, loaderModel);
            req.getRequestDispatcher("/WEB-INF/jsp/loader.jsp").forward(req, resp);
        }
    }

    private void handleCursors(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        CursorsModel cursorsModel = modelFactory.getCursorsModel();
        req.setAttribute(MODEL_ARG, cursorsModel);
        req.getRequestDispatcher("/WEB-INF/jsp/cursors.jsp").forward(req, resp);
    }

    private void handleCursor(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String idParam = req.getPathInfo().substring(1);
        Long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            redirectWithMessage(req, resp, "/cursors", ALERT_TYPE_DANGER, "Invalid cursor ID format.");
            return;
        }
        CursorModel cursorModel = modelFactory.getCursorModel(id);
        if (cursorModel.getCursor() == null) {
            redirectWithMessage(req, resp, "/cursors", ALERT_TYPE_DANGER, "Unknown cursor with id " + id + ".");
        } else {
            req.setAttribute("model", cursorModel);
            req.getRequestDispatcher("/WEB-INF/views/cursor.jsp").forward(req, resp);
        }
    }

    private void redirectWithMessage(HttpServletRequest req, HttpServletResponse resp, String path, String alertType, String message) throws IOException {
        req.getSession().setAttribute(ALERT_TYPE_ARG, alertType);
        req.getSession().setAttribute(ALERT_MSG_ARG, message);
        resp.sendRedirect(req.getContextPath() + path);
    }

    private void handleConnections(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        ConnectionsModel connectionsModel = modelFactory.getConnectionsModel();
        req.setAttribute(MODEL_ARG, connectionsModel);
        req.getRequestDispatcher("/WEB-INF/jsp/connections.jsp").forward(req, resp);
    }

    private void handleLocks(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        LocksModel locksModel = modelFactory.getLocksModel();
        req.setAttribute(MODEL_ARG, locksModel);
        req.getRequestDispatcher("/WEB-INF/jsp/locks.jsp").forward(req, resp);
    }

    private void handleTrack(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String param = req.getPathInfo().substring(1);
        boolean trackMessages = Boolean.parseBoolean(param);
        ((TBMonitor) AbstractHandler.TDB).setTrackMessages(trackMessages);
        String referer = req.getHeader("Referer");
        resp.sendRedirect(referer != null ? referer : req.getContextPath() + "/");
    }

    private void handleNotFound(HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
    }
}