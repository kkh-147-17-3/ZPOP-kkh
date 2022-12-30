package com.zpop.web.service;

import com.zpop.web.dao.MeetingDao;
import com.zpop.web.dao.MemberDao;
import com.zpop.web.dao.ParticipationDao;
import com.zpop.web.dto.MeetingThumbnailPagination;
import com.zpop.web.dto.MeetingThumbnailResponse;
import com.zpop.web.entity.Member;
import com.zpop.web.entity.Participation;
import com.zpop.web.entity.meeting.Meeting;
import com.zpop.web.entity.meeting.MeetingThumbnailView;
import com.zpop.web.utils.TextDateTimeCalculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.zpop.web.dao.CategoryDao;

import com.zpop.web.dto.MeetingDetailDto;
import com.zpop.web.dto.MeetingParticipantsDto;


@Service
public class DefaultMeetingService implements MeetingService{

    @Autowired
    private MeetingDao dao;

    @Autowired
	private ParticipationDao participationDao;
    
    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private MemberDao memberDao;
    
    public DefaultMeetingService() {
    }

    public DefaultMeetingService(MeetingDao dao, ParticipationDao participationDao){
        this.dao = dao;
        this.participationDao = participationDao;

    }

    @Override
    public List<MeetingThumbnailResponse> getList() {
        return getList(
            0,
            null,
            null,
            null,
            false
        );
    }

    @Override
    public List<MeetingThumbnailResponse> getList(String keyword) {
        return getList(
            0,
            keyword,
            null,
            null,
            false
        );
    }

    @Override
    public List<MeetingThumbnailResponse> getList(
            int startId, String keyword, Integer categoryId, String strRegionIds, Boolean isClosed
            ) {
        String[] regionIds = null;
        if(strRegionIds != null)
            regionIds = strRegionIds.split(",");

        MeetingThumbnailPagination pagination = 
            new MeetingThumbnailPagination(startId, keyword, categoryId, regionIds, isClosed);

        List<MeetingThumbnailView> meetingThumbnailViews = dao.getThumbnailViewList(pagination);
        
        // 응답에 맞게 데이터 변환
        List<MeetingThumbnailResponse> list = new ArrayList<>();
        for(MeetingThumbnailView m : meetingThumbnailViews) {
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
            
            MeetingThumbnailResponse meetingThumbnail = new MeetingThumbnailResponse(
                m.getId(),
                m.getCategory(),
                m.getRegion(),
                m.getAgeRange(),
                genderCategory,
                m.getMaxMember(),
                m.getTitle(),
                dateTime,
                m.getViewCount(),
                m.getCommentCount(),
                isClosedResult
            );

            list.add(meetingThumbnail);
        }

        return list;
    }

    
    
	@Override
	public int register(Meeting meeting) {
		return dao.insert(meeting);
	}

//	public int participate(Participation participation) {
//		// 주최자가 참여한 경우 -> host ID랑 MemberId랑 같을 경우
//		// 참여하기를 눌렀는데 모임의 아이디가 없을 경우
//		// 강퇴당한 사용자일 경우
//		// 마감된 모임일 경우
//		//		Participation participation = new Participation(participation);
//		return participationDao.insert(participation);
//	}

	@Override
	public MeetingDetailDto getById(int id) {
		return dao.getDetailView(id);
	}

	@Override
	public List<MeetingParticipantsDto> getParticipants(int meetingId) {
		
		return participationDao.getByMeetingId(meetingId);
	}

    @Override
    public boolean delete(int id, Member member) {

        Meeting foundMeeting = dao.get(id);

        if(foundMeeting == null || foundMeeting.getDeletedAt() != null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 모임입니다");

        int memberId = member.getId();

        if(foundMeeting.getRegMemberId() != memberId)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다");

        List<Participation> participations = participationDao.getListByMeetingId(id);

        for(Participation p : participations) {
            // 주최자 자기 자신 제외
            if(p.getParticipantId() == memberId)
                continue;

            Date bannedAt = p.getBannedAt();
            Date canceledAt = p.getCanceledAt();

            // 정상 참가자가 한명이라도 있으면 삭제 불가
            if(bannedAt == null && canceledAt == null )
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "참가자가 있어 모임을 삭제할 수 없습니다");
        }

        dao.updateDeletedAt(foundMeeting);

        return true;
    }
	@Override
	public void updateViewCount(int id) {
		dao.updateViewCount(id);
		
	}

    @Override
    public boolean kick(int id, int participantId, Member member) {
        Meeting foundMeeting = dao.get(id);

        if(foundMeeting == null || foundMeeting.getDeletedAt() != null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 모임입니다");
        
        int memberId = member.getId();

        if(foundMeeting.getRegMemberId() != memberId)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다");

        List<Participation> participations = participationDao.getListByMeetingId(id);

        Participation kickTarget = null;

        for(Participation p: participations) {
            if(p.getParticipantId() == participantId) {
                kickTarget = p;
                break;
            }
        }

        if(kickTarget == null || kickTarget.getCanceledAt() != null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "모임에 참여하지 않는 회원입니다");

        if(kickTarget.getBannedAt() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 강퇴된 회원입니다");

        // 탈퇴한 회원인지 확인
        int kickTargetMemberId = kickTarget.getParticipantId();
        Member kickTargetMember = memberDao.getById(kickTargetMemberId);
        if(kickTargetMember.getResignedAt() != null) {
            //TODO: dao.참여취소(kickTargetId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다");
        }

        // 자기자신을 강퇴하려 할 때
        if(kickTargetMemberId == member.getId())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자기 자신을 내보낼 수 없습니다");

        participationDao.updateBannedAt(kickTarget.getId());
        
        return true;
    }

    @Override
    public boolean close(int id, Member member) {

        Meeting foundMeeting = dao.get(id);

        if(foundMeeting == null || foundMeeting.getDeletedAt() != null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 모임입니다");
        
        int memberId = member.getId();

        if(foundMeeting.getRegMemberId() != memberId)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다");
        
        if(foundMeeting.getClosedAt() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 마감된 모임입니다");
        
        // 모임 시작일이 지났을 때 -> 마감 후 예외
        Date startedAt = foundMeeting.getStartedAt();
        if(startedAt.before(new Date())) {
            dao.updateClosedAt(foundMeeting);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 마감된 모임입니다");
        }

        dao.updateClosedAt(foundMeeting);

        return true;
    }

	@Override
	public int participate(int meetingId, int memberId) {
		// 주최자가 참여한 경우 -> host ID랑 MemberId랑 같을 경우
				// 참여하기를 눌렀는데 모임의 아이디가 없을 경우
				// 강퇴당한 사용자일 경우
				// 마감된 모임일 경우
//				Participation participation = new Participation(participation);
		
		
				int maxNumber = dao.getMaxMember(meetingId);
				System.out.println(maxNumber);
				return participationDao.insert(meetingId, memberId);
	}

    @Override
    public boolean cancelParticipate(int id, int memberId) {

        // 모임이 존재하는지 확인한다
        Meeting foundMeeting = dao.get(id);

        if(foundMeeting == null || foundMeeting.getDeletedAt() != null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 모임입니다");
        
        // 참여자가 맞는지 확인한다
        Participation participationInfo = null;

        List<Participation> participations = participationDao.getListByMeetingId(id);
        for(Participation p : participations) {
            if(p.getParticipantId() == memberId) {
                participationInfo = p;
                break;
            }
        }

        /*
         * 1. 모임에 참여한 적이 없을 때
         * 2. 취소한 모임에 참여 취소할 때
         * 3. kick당한 모임에 참여 취소할 때
         */
        if(participationInfo == null ||
            participationInfo.getCanceledAt() != null ||
            participationInfo.getBannedAt() != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "참여한 모임에만 참여를 취소할 수 있습니다");

        // 마감된 모임은 참여를 취소할 수 없다
        Date currentTime = new Date();
        Date meetingStartedAt = foundMeeting.getStartedAt();

        if(foundMeeting.getClosedAt() != null ||
            currentTime.after(meetingStartedAt))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "마감된 모임에 참여를 취소할 수 없습니다");
        
        participationDao.updateCanceledAt(participationInfo.getId());

        return true;
    }
}


