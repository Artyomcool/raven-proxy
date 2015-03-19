package org.artyomcool.ravenproxy;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import net.kencochrane.raven.Raven;
import org.artyomcool.ravenproxy.data.FingerPrint;
import org.artyomcool.ravenproxy.data.SentryEvent;
import org.artyomcool.ravenproxy.data.SentryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/app/{app}/version/{version}")
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    @Autowired
    private StacktraceDecoder decoder;

    @Autowired
    private RavenCache ravenCache;

    private Gson gson = new Gson();

    @RequestMapping(value = "/crash", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void crash(
            @PathVariable("app") String app,
            @PathVariable("version") String version,
            @RequestBody SentryEvent crash,
            HttpServletRequest request) {

        FingerPrint fingerPrint = new FingerPrint(app, version);
        Raven raven = getRaven(fingerPrint);

        List<SentryException> exceptions = decoder.decode(fingerPrint, crash.getException());
        crash.setException(exceptions);

        addMeta(crash, version, request.getRemoteAddr());

        raven.sendEvent(gson.toJson(crash).getBytes(Charsets.UTF_8));
    }

    @RequestMapping(value = "/event", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void event(
            @PathVariable("app") String app,
            @PathVariable("version") String version,
            @RequestBody SentryEvent event,
            HttpServletRequest request) {

        FingerPrint fingerPrint = new FingerPrint(app, version);
        Raven raven = getRaven(fingerPrint);

        try {
            addMeta(event, version, request.getRemoteAddr());
        } catch (Exception ignored) {
            logger.error("Can't add meta", ignored);
        }

        raven.sendEvent(gson.toJson(event).getBytes(Charsets.UTF_8));
    }

    @RequestMapping(value="/mapping", method=RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void uploadMapping(
            @PathVariable("app") String app,
            @PathVariable("version") String version,
            HttpServletRequest request) throws IOException {

        File tmpDir = new File("tmpDir");
        if (!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw new IOException("Can't create tmp directory");
        }

        File file = File.createTempFile("mapping", ".tmp", tmpDir);
        FileOutputStream out = new FileOutputStream(file);
        FileCopyUtils.copy(request.getInputStream(), out);
        out.close();

        File dest = new File(new FingerPrint(app, version).toFileName());
        if (dest.exists()) {
            if (!dest.delete()) {
                throw new IOException("Can't delete old mapping");
            }
        } else {
            File parentFile = dest.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw new IOException("Can't rename file");
            }
        }
        if (!file.renameTo(dest)){
            throw new IOException("Can't rename tmp file");
        }
    }

    private Raven getRaven(FingerPrint fingerPrint) {
        Raven raven = ravenCache.forFingerPrint(fingerPrint);
        if (raven == null) {
            throw new IllegalArgumentException("Unknown application: " + fingerPrint);
        }
        return raven;
    }

    private void addMeta(SentryEvent event, String version, String ipAddress) {
        event.getUser().setIpAddress(ipAddress);
        Map<String, String> tags = event.getTags();
        if (tags == null) {
            tags = new HashMap<>();
            event.setTags(tags);
        }
        tags.put("version", version);
    }


}
