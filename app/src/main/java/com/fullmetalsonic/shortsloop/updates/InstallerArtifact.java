package com.fullmetalsonic.shortsloop.updates;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;

/** Immutable per-digest install copy, so a later download cannot replace the installer's open URI. */
final class InstallerArtifact {
    private InstallerArtifact() { }
    static synchronized File prepare(Context context, UpdateManifest item) throws IOException {
        File source = UpdateFileProvider.apk(context);
        ApkVerifier.verify(context, source, item);
        File target = UpdateFileProvider.installFile(context, item.sha256);
        if (target.exists()) {
            try { ApkVerifier.verify(context, target, item); return target; }
            catch (IOException invalid) { /* A interrupted old copy must not block retry forever. */ }
        }
        File pending = new File(target.getParentFile(), target.getName() + ".part");
        try {
            Files.copy(source.toPath(), pending.toPath(), StandardCopyOption.REPLACE_EXISTING);
            ApkVerifier.verify(context, pending, item);
            try { Files.move(pending.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException unavailable) {
                Files.move(pending.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return target;
        } finally { Files.deleteIfExists(pending.toPath()); }
    }
}
