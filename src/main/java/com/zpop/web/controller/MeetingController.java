package com.zpop.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zpop.web.entity.comment.Comment;
import com.zpop.web.entity.comment.CommentView;
import com.zpop.web.service.CommentService;
/*
 * 작성자: 임형미 & 
 */
@Controller
@RequestMapping("/meeting")
public class MeetingController {
	@Autowired
	CommentService service;
	
	@GetMapping("/register")
	public String registerView() {
		// TODO: 인가처리
		return "meeting/register";
	}

	@GetMapping("/{id}")
	public String detailView(@PathVariable int id, Model model) {
		
		List<CommentView> comments = service.getComment(id);
		int countOfComment = service.getCountOfComment(id);
		model.addAttribute("meetingId", id);
		model.addAttribute("comments", comments);
		model.addAttribute("countOfComment", countOfComment);

		return "meeting/detail2";
	}
	
	@PostMapping("comment")
	@ResponseBody
	public  Map<String, Object> getComment(@RequestBody Comment comment) {
		
		List<CommentView> comments = service.getComment(comment.getMeetingId());
		int countOfComment = service.getCountOfComment(comment.getMeetingId());
		
		Map<String,Object> dto = new HashMap<>();
		dto.put("status",200);
		dto.put("resultObject",comments);
		dto.put("countOfComment",countOfComment);
		
		return dto;
	}
	
	@PostMapping("reply")
	@ResponseBody
	public  Map<String, Object> getReply(@RequestBody Comment comment) {

		List<CommentView> replies = service.getReply(comment.getGroupId());
		int countOfReply = service.getCountOfReply(comment.getGroupId());
		
		Map<String,Object> dto = new HashMap<>();
		dto.put("status",200);
		dto.put("resultObject",replies);
		dto.put("countOfReply",countOfReply);
		
		return dto;
	}
	
}