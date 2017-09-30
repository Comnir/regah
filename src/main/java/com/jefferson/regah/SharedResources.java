package com.jefferson.regah;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class SharedResources {
    final Set<File> resources = new HashSet<>();

    public Set<File> getResources() {
        return resources;
    }

    public void share(File file) {
        if () {
            resources.add(file);
        }
    }

    public void unshare(File file) {
        resources.remove(file);
    }
}
