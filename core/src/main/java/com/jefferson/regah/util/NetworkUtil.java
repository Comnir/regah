package com.jefferson.regah.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

public class NetworkUtil {
    private static final Logger log = LogManager.getLogger(NetworkUtil.class);

    public static InetAddress addressForBinding() {
        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            final List<NetworkInterface> candidatesNotUp = new ArrayList<>();
            final List<NetworkInterface> candidatesUp = new ArrayList<>();
            final List<NetworkInterface> virutalInterfaces = new ArrayList<>();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback()) {
                    log.debug("Skipping loopback interface:{}", networkInterface);
                    continue;
                }

                if (!networkInterface.isUp()) {
                    candidatesNotUp.add(networkInterface);
                    continue;
                }

                if (networkInterface.isVirtual()) {
                    virutalInterfaces.add(networkInterface);
                    continue;
                }

                candidatesUp.add(networkInterface);
            }

            candidatesNotUp
                    .forEach(notUp -> log.debug("Network interface down: {}", notUp));
            virutalInterfaces
                    .forEach(virtual -> log.debug("Virtual network interface : {}", virtual));


            return Optional.of(candidatesUp)
                    .filter(is -> 1 == is.size())
                    .map(is -> is.get(0))
                    .stream()
                    .flatMap(NetworkInterface::inetAddresses)
                    .filter(i -> i instanceof Inet4Address)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Failed to pick an IP for binding." +
                            " This might be caused by no matching candidates or more than one candidate."));
        } catch (SocketException e) {
            throw new IllegalStateException("Failed to find network interface to bind to.");
        }
    }

    public static void main(String[] args) {
        System.out.println("Address for binding: " + addressForBinding());
    }
}