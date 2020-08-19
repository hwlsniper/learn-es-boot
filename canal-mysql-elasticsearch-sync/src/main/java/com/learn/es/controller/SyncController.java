package com.learn.es.controller;

import com.learn.es.model.request.SyncByTableRequest;
import com.learn.es.model.response.Response;
import com.learn.es.service.SyncService;
import com.learn.es.util.JsonUtil;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.annotation.Resource;

@Log4j2
@Controller
@RequestMapping("/sync")
public class SyncController {

    @Resource
    private SyncService syncService;

    /**
     * 通过库名和表名全量同步数据
     *
     * @param request 请求参数
     */
    @RequestMapping("/byTable")
    @ResponseBody
    public String syncTable(@Validated SyncByTableRequest request) {
        log.debug("request_info: " + JsonUtil.toJson(request));
        String response = Response.success(syncService.syncByTable(request)).toString();
        log.debug("response_info: " + JsonUtil.toJson(request));
        return response;
    }
}
