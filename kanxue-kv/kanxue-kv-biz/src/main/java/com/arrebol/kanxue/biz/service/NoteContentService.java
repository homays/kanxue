package com.arrebol.kanxue.biz.service;

import com.arrebol.framework.common.response.Response;
import com.arrebol.kanxue.biz.dto.req.AddNoteContentReqDTO;

public interface NoteContentService {

    /**
     * 添加笔记内容
     */
    Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO);

}