package edu.cmu.project4.server.web;

import edu.cmu.project4.server.config.AppAttributes;
import edu.cmu.project4.server.data.MongoLogRepository;
import edu.cmu.project4.server.data.RequestLog;
import edu.cmu.project4.server.data.SymbolStats;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Renders the operations dashboard with analytics and logs.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends HttpServlet {
    private transient MongoLogRepository repository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.repository = (MongoLogRepository) config.getServletContext().getAttribute(AppAttributes.MONGO_REPOSITORY);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long total = repository.totalCount();
        long success = repository.successCount();
        double avgLatency = repository.averageLatencyMs();
        List<SymbolStats> topSymbols = repository.topSymbols(5);
        List<RequestLog> recentLogs = repository.recentLogs(50);

        double successRate = total == 0 ? 0.0 : (double) success / (double) total * 100.0;

        req.setAttribute("totalRequests", total);
        req.setAttribute("successRate", successRate);
        req.setAttribute("avgLatency", avgLatency);
        req.setAttribute("topSymbols", topSymbols);
        req.setAttribute("recentLogs", recentLogs);

        req.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp").forward(req, resp);
    }
}
