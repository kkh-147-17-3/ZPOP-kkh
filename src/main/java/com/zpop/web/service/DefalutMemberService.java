package com.zpop.web.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zpop.web.dao.MeetingDao;
import com.zpop.web.dao.MemberDao;
import com.zpop.web.dao.MemberEvalDao;
import com.zpop.web.dao.ParticipationDao;
import com.zpop.web.dto.EvalDto;
import com.zpop.web.dto.EvalMemberDto;
import com.zpop.web.dto.MyMeetingResponse;
import com.zpop.web.dto.ProfileResponse;
import com.zpop.web.entity.Member;
import com.zpop.web.entity.MemberEval;
import com.zpop.web.entity.Participation;
import com.zpop.web.entity.member.MyMeetingView;
import com.zpop.web.utils.TextDateTimeCalculator;

@Service
public class DefalutMemberService implements MemberService {

	@Autowired
    private MemberDao dao;   
    @Autowired
    private MeetingDao meetingDao;
    @Autowired
    private ParticipationDao participationDao;
    @Autowired
    private MemberEvalDao memberEvalDao;


    public DefalutMemberService() {
    }

    public DefalutMemberService(MemberDao dao, MeetingDao meetingDao, ParticipationDao participationDao) {
        this.dao = dao;
        this.meetingDao = meetingDao;
        this.participationDao = participationDao;
    }


    @Override
    public Member getById(int id) {
        return dao.getById(id);
    }

    @Override
    public List<MyMeetingResponse> getMyMeeting(int memberId) {
        List<MyMeetingView> mmv = dao.getMyMeeting(memberId);

        List<MyMeetingResponse> list = new ArrayList<>();

        for (MyMeetingView m : mmv) {
            String genderCategory = "누구나";
            switch (m.getGenderCategory()) {
                case 1:
                    genderCategory = "남자 모임";
                    break;
                case 2:
                    genderCategory = "여자 모임";
                    break;
            }

            String dateTime = TextDateTimeCalculator.getTextDateTime(m.getStartedAt());

            boolean isClosedResult = false;
            if(m.getClosedAt() != null)
                isClosedResult = true;

            Date date = m.getStartedAt();
            LocalDate localDate = new java.sql.Date(date.getTime()).toLocalDate();
            LocalDate now =LocalDate.now();
            boolean canRate = now.isAfter(localDate);

            MyMeetingResponse mt = new MyMeetingResponse(
                    m.getCategoryName(),
                    m.getRegionName(),
                    dateTime,
                    m.getTitle(),
                    m.getAge(),
                    m.getMaxMember(),
                    genderCategory,
                    isClosedResult,
                    m.getViewCount(),
                    m.getCommentCount(),
                    m.getMeetingId(),
                    m.getParticipantId(),
                    m.getRegMemberId(),
                    canRate
            );

            
            list.add(mt);

        }

        return list;

    }
    @Override
    public List<MyMeetingResponse> getMyGathering(int memberId) {

        List<MyMeetingView> mmv = dao.getMyGathering(memberId);

        List<MyMeetingResponse> list = new ArrayList<>();

        for (MyMeetingView m : mmv) {
            String genderCategory = "누구나";
            switch (m.getGenderCategory()) {
                case 1:
                    genderCategory = "남자 모임";
                    break;
                case 2:
                    genderCategory = "여자 모임";
                    break;
            }

            String dateTime = TextDateTimeCalculator.getTextDateTime(m.getStartedAt());

            boolean isClosedResult = false;
            if(m.getClosedAt() != null)
                isClosedResult = true;

            Date date = m.getStartedAt();
            LocalDate localDate = new java.sql.Date(date.getTime()).toLocalDate();
            LocalDate now =LocalDate.now();
            boolean canRate = now.isAfter(localDate);

            MyMeetingResponse mt = new MyMeetingResponse(
                    m.getCategoryName(),
                    m.getRegionName(),
                    dateTime,
                    m.getTitle(),
                    m.getAge(),
                    m.getMaxMember(),
                    genderCategory,
                    isClosedResult,
                    m.getViewCount(),
                    m.getCommentCount(),
                    m.getMeetingId(),
                    m.getParticipantId(),
                    m.getRegMemberId(),
                    canRate
            );
            
            list.add(mt);
        }

        return list;
    }

    @Override
    public List<EvalMemberDto> getEvalMember(int meetingId) {
       
        return  dao.getEvalMember(meetingId);
    }

    @Override
    @Transactional
    public Map<String, Object> getRateData(EvalDto dto) {
        List<MemberEval> evals = dto.getEvals();
        for (MemberEval eval : evals) {
            eval.setEvaluatorId(dto.getEvaluatorId());
            eval.setMeetingId(dto.getMeetingId());
         }
        memberEvalDao.insertAll(evals);
        participationDao.updateHasEvaluated(dto.getMeetingId(), dto.getEvaluatorId());
        System.out.println(evals);
        dao.updateFameAll(evals);
        return null;
    }

    @Override
    public ProfileResponse getParticipant(int id) {
        
        // id 로 해당 사용자를 찾는다
        Member member = getById(id);
        // 만약 id에 해당 사용자가 없다면 404 예외를 얻는다
        // 만약 탈퇴한 회원이라면 바로 응답하여 탈퇴한 회원임을 사용자에게 보여준다.
        if(member==null)
            System.out.println("탈퇴한 회원입니다");
        
        boolean isResigned;
        if(member.getResignedAt()!=null)
            isResigned = true;
        else 
            isResigned = false;

        // 찾아온 멤버 객체에서 id, nickname, fame , imgPath 추출한다

        // 참여 Dao 로 해당 사용자의 참여 리스트 모두 가져온다.
        List<Participation> participationList = participationDao.getListByMeetingId(id);

        int participatedMeetingNumber=0;
        for(Participation p : participationList)
            if(p.getBannedAt()!=null && p.getCanceledAt()!=null)
                participatedMeetingNumber++;
        
        // 참여한 모임이 0 개인 경우 바로 응답
        if(participatedMeetingNumber!=0)
            System.out.println("banned, canceled meetings");
        // 밴, 취소한 모임은 개수는 제외한다.

       

        ProfileResponse participant = new ProfileResponse(
            member.getId(),
            member.getNickname(),
            member.getFame(),
            member.getProfileImagePath(),
            isResigned,
            participatedMeetingNumber
        );

        return participant;
    }
}
