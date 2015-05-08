package org.artyomcool.ravenproxy.impl;

import com.github.artyomcool.retrace.Line;
import com.github.artyomcool.retrace.Retrace;
import com.github.artyomcool.retrace.StackTrace;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.artyomcool.ravenproxy.StacktraceDecoder;
import org.artyomcool.ravenproxy.data.FingerPrint;
import org.artyomcool.ravenproxy.data.SentryException;
import org.artyomcool.ravenproxy.data.SentryStackFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ProguardStacktraceDecoder implements StacktraceDecoder {

    private static final Logger logger = LoggerFactory.getLogger(ProguardStacktraceDecoder.class);

    private final LoadingCache<FingerPrint, Retrace> retraceCache = CacheBuilder.newBuilder()
            .concurrencyLevel(20)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .initialCapacity(100)
            .maximumSize(1000)
            .build(new CacheLoader<FingerPrint, Retrace>() {
                @Override
                public Retrace load(FingerPrint key) throws Exception {
                    try (BufferedReader mappings = new BufferedReader(new FileReader(key.toFileName()))) {
                        return new Retrace(mappings);
                    }
                }
            });

    @Override
    public List<SentryException> decode(FingerPrint fingerPrint, List<SentryException> stacktrace) {
        if (stacktrace == null || stacktrace.isEmpty()) {
            return stacktrace;
        }
        try {
            Retrace retrace = retraceCache.get(fingerPrint);
            StackTrace obfuscated = toStackTrace(retrace, stacktrace);
            StackTrace deobfuscated = retrace.stackTrace(obfuscated);

            return toSentryExceptions(deobfuscated);
        } catch (ExecutionException e) {
            try {
                throw e.getCause();
            } catch (FileNotFoundException e1) {
                logger.info("no retrace found for {}", fingerPrint);
                return stacktrace;
            } catch (Throwable t) {
                logger.error("can't decode stacktrace", e);
                return stacktrace;
            }
        }
    }

    private StackTrace toStackTrace(Retrace retrace, List<SentryException> stacktrace) {
        StackTrace result = new StackTrace();
        StackTrace cause = result;

        Iterator<SentryException> iterator = stacktrace.iterator();
        SentryException currentStackTrace = iterator.next();

        for (;;) {
            cause.setMessage(currentStackTrace.getValue());
            cause.setException(retrace.className(currentStackTrace.getModule() + "." + currentStackTrace.getType()));
            cause.setLines(toLines(currentStackTrace.getStackTrace()));

            if (!iterator.hasNext()) {
                return result;
            }

            currentStackTrace = iterator.next();

            StackTrace nextCause = new StackTrace();
            cause.setCausedBy(nextCause);
            cause = nextCause;
        }
    }

    private List<Line> toLines(List<SentryStackFrame> frames) {
        List<Line> lines = new ArrayList<>(frames.size());
        for (SentryStackFrame frame : frames) {
            lines.add(toLine(frame));
        }
        return lines;
    }

    private Line toLine(SentryStackFrame frame) {
        Line line = new Line();
        line.setClassName(frame.getModule());
        line.setMethodName(frame.getFunction());
        line.setLineNumber(frame.getLineNumber());
        return line;
    }

    private List<SentryException> toSentryExceptions(StackTrace stacktrace) {
        List<SentryException> result = new ArrayList<>();
        do {
            SentryException exception = new SentryException();
            String className = stacktrace.getException();
            String packageName = "";
            int pos = className.lastIndexOf('.');
            if (pos != -1) {
                packageName = className.substring(0, pos);
                className = className.substring(pos + 1);
            }
            exception.setValue(stacktrace.getMessage());
            exception.setType(className);
            exception.setModule(packageName);
            exception.setStackTrace(fromLines(stacktrace.getLines()));

            result.add(exception);

            stacktrace = stacktrace.getCausedBy();
        } while (stacktrace != null);

        return result;
    }

    private List<SentryStackFrame> fromLines(List<Line> lines) {
        List<SentryStackFrame> result = new ArrayList<>();
        for (Line line : lines) {
            result.add(fromLine(line));
        }
        return result;
    }

    private SentryStackFrame fromLine(Line line) {
        SentryStackFrame frame = new SentryStackFrame();
        frame.setModule(line.getClassName());
        frame.setFunction(line.getMethodName());
        frame.setLineNumber(line.getLineNumber());
        return frame;
    }

}
