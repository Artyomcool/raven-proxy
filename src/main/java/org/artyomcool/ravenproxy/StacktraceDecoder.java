package org.artyomcool.ravenproxy;

import org.artyomcool.ravenproxy.data.FingerPrint;
import org.artyomcool.ravenproxy.data.SentryException;

import java.util.List;

public interface StacktraceDecoder {

    List<SentryException> decode(FingerPrint print, List<SentryException> stacktrace);

}
