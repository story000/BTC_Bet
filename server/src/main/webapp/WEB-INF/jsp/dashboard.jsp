<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="edu.cmu.project4.server.data.RequestLog" %>
<%@ page import="edu.cmu.project4.server.data.SymbolStats" %>
<%
    long totalRequests = (long) request.getAttribute("totalRequests");
    double successRate = (double) request.getAttribute("successRate");
    double avgLatency = (double) request.getAttribute("avgLatency");
    List<SymbolStats> topSymbols = (List<SymbolStats>) request.getAttribute("topSymbols");
    List<RequestLog> recentLogs = (List<RequestLog>) request.getAttribute("recentLogs");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Crypto Monitor Dashboard</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 16px; }
        h1 { margin-bottom: 8px; }
        .metrics { display: flex; gap: 16px; margin-bottom: 24px; }
        .card { border: 1px solid #ccc; border-radius: 6px; padding: 12px 16px; min-width: 180px; background: #f5f5f5; }
        table { border-collapse: collapse; width: 100%; margin-top: 16px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background: #fafafa; }
        .section { margin-top: 32px; }
    </style>
</head>
<body>
<h1>Crypto Monitor Operations Dashboard</h1>
<div class="metrics">
    <div class="card">
        <div>Total Requests</div>
        <div style="font-size: 1.5em; font-weight: bold;"><%= totalRequests %></div>
    </div>
    <div class="card">
        <div>Success Rate</div>
        <div style="font-size: 1.5em; font-weight: bold;"><%= String.format("%.1f%%", successRate) %></div>
    </div>
    <div class="card">
        <div>Avg Response Latency</div>
        <div style="font-size: 1.5em; font-weight: bold;"><%= String.format("%.0f ms", avgLatency) %></div>
    </div>
</div>

<div class="section">
    <h2>Top Requested Symbols</h2>
    <table>
        <tr>
            <th>Symbol</th>
            <th>Request Count</th>
        </tr>
        <%
            for (SymbolStats stats : topSymbols) {
        %>
        <tr>
            <td><%= stats.getSymbol() %></td>
            <td><%= stats.getCount() %></td>
        </tr>
        <%
            }
        %>
    </table>
</div>

<div class="section">
    <h2>Recent Logs</h2>
    <table>
        <tr>
            <th>Time</th>
            <th>Request ID</th>
            <th>Client</th>
            <th>Symbol</th>
            <th>Price</th>
            <th>Binance Status</th>
            <th>Binance Latency (ms)</th>
            <th>Total Latency (ms)</th>
            <th>Status</th>
            <th>Message</th>
        </tr>
        <%
            for (RequestLog log : recentLogs) {
        %>
        <tr>
            <td><%= log.getRequestReceivedAt() %></td>
            <td><%= log.getRequestId() %></td>
            <td><%= log.getClientId() == null ? log.getClientIp() : log.getClientId() %></td>
            <td><%= log.getSymbol() %></td>
            <td><%= log.getPrice() == null ? "-" : log.getPrice().toPlainString() %></td>
            <td><%= log.getBinanceStatus() %></td>
            <td><%= log.getBinanceLatencyMs() %></td>
            <td><%= log.getTotalLatencyMs() %></td>
            <td><%= log.isSuccess() ? "OK" : "Failed" %></td>
            <td><%= log.getErrorMessage() == null ? "" : log.getErrorMessage() %></td>
        </tr>
        <%
            }
        %>
    </table>
</div>
</body>
</html>
