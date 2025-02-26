package com.seleniumtests.util.har;

public class Timing {
    private double blocked;
    private double dns;
    private double connect;
    private double ssl;
    private double send;
    private double wait;
    private double receive;

    public Timing(double blocked, double dns, double connect, double ssl, double send, double wait, double receive) {
        this.blocked = blocked;
        this.dns = dns;
        this.connect = connect;
        this.ssl = ssl;
        this.send = send;
        this.wait = wait;
        this.receive = receive;
    }

    public double getBlocked() {
        return blocked;
    }

    public double getDns() {
        return dns;
    }

    public double getConnect() {
        return connect;
    }

    public double getSsl() {
        return ssl;
    }

    public double getSend() {
        return send;
    }

    public double getWait() {
        return wait;
    }

    public double getReceive() {
        return receive;
    }
}
