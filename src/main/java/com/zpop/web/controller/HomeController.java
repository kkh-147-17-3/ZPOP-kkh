package com.zpop.web.controller;

import com.zpop.web.dto.MeetingThumbnailResponse;
import com.zpop.web.entity.MeetingFile;
import com.zpop.web.service.MeetingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private MeetingService meetingService;

    public HomeController() {
    }

    public HomeController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @RequestMapping (value={"/","/{path:[^\\.]*}"}) // 루트 (index.html을 위해) 혹은 web 접근시에 Vue 프로젝트의 view 호출
    public String index() {
        return "index.html";
    }

    @GetMapping("/search/{keyword}")
    public String searchView(Model model, @PathVariable String keyword) {
        List<MeetingThumbnailResponse> meetings = meetingService.getList(keyword);

        model.addAttribute("meetings", meetings);

        return "meeting/search";
    }
    
    @PostMapping("/api/upload")
	@ResponseBody
	public ResponseEntity<?> uploadFile(@NotNull MultipartFile file 
							, @NotNull @NotEmpty String path
							,HttpServletRequest request) throws IOException{
		String realPath = "/usr/zpop" + path;
		MeetingFile meetingFile = meetingService.uploadFile(file, realPath);
		System.out.println(meetingFile);
		return ResponseEntity.ok(meetingFile);
	}
}
