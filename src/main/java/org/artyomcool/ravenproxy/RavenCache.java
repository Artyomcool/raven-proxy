package org.artyomcool.ravenproxy;

import net.kencochrane.raven.Raven;
import org.artyomcool.ravenproxy.data.FingerPrint;

public interface RavenCache {

    Raven forFingerPrint(FingerPrint fingerPrint);

}
