package com.zpop.web.service;

import com.zpop.web.dao.*;
import com.zpop.web.dto.EvalDto;
import com.zpop.web.dto.EvalMemberDto;
import com.zpop.web.dto.MyMeetingResponse;
import com.zpop.web.dto.ProfileResponse;
import com.zpop.web.entity.Member;
import com.zpop.web.entity.MemberEval;
import com.zpop.web.entity.NicknameLog;
import com.zpop.web.entity.Participation;
import com.zpop.web.entity.member.MyMeetingView;
import com.zpop.web.utils.TextDateTimeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.regex.Pattern;

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

    @Autowired
    private NicknameLogDao nicknameLogDao;

    private final int MAX_NICKNAME_LENGTH = 10;
	private final String nicknamePattern = "^(?=.*[a-zA-Z0-9가-힣])[a-zA-Z0-9가-힣]{0,10}$";

    public DefalutMemberService() {
    }

    public DefalutMemberService(MemberDao dao, MeetingDao meetingDao, ParticipationDao participationDao) {
        this.dao = dao;
        this.meetingDao = meetingDao;
        this.participationDao = participationDao;
    }


    /***
     * 회원 기본정보 응답
     * @param id
     * @return
     */
    @Override
    public Member getById(int id) {
        return dao.getById(id);
    }

    /**
     * 내가 참여한 모임 응답
     * @param memberId
     * @return
     */
    @Override
    public List<MyMeetingResponse> getMyMeeting(int memberId) {
        List<MyMeetingView> mmv = dao.getMyMeeting(memberId);

        List<MyMeetingResponse> list = new ArrayList<>();

        for (MyMeetingView m : mmv) {
            String genderCategory = "남녀 모두";
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
            boolean isEvaluated;
            if(m.getHasEvaluated() == 1) {
                isEvaluated = true;
            }else {
                isEvaluated = false;
            }


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
                    canRate,
                    isEvaluated
            );

            
            list.add(mt);

        }

        return list;

    }


    /***
     * 내가 모집한 모임 응답
     * @param memberId
     * @return
     */
    @Override
    public List<MyMeetingResponse> getMyGathering(int memberId) {

        List<MyMeetingView> mmv = dao.getMyGathering(memberId);

        List<MyMeetingResponse> list = new ArrayList<>();

        for (MyMeetingView m : mmv) {
            String genderCategory = "남녀 모두";
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

            boolean isEvaluated;
            if(m.getHasEvaluated() == 1) {
                isEvaluated = true;
            }else {
                isEvaluated = false;
            }

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
                    canRate,
                    isEvaluated
            );

            list.add(mt);
        }

        return list;
    }

    /***
     * TODO : 주최자 혼자인채로 모임이 마감된 경우, 평가하기 비활성화
     * 평가하기 클릭시, 해당 모임에 참여한 회원을 불러옵니다.
     * @param meetingId
     * @return
     */
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

        int  result = participationDao.updateHasEvaluated(dto.getMeetingId(), dto.getEvaluatorId());
        System.out.println(result);
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


    /**
     * 닉네임 유효성 검사 (중복, 유효한 닉네임인지)
     * @param nickname
     * @return
     */
    public Map<String, Object> checkNicknameValid(String nickname) {
		Map<String, Object> result = new HashMap<>();
		boolean isRegexMatch = Pattern.matches(nicknamePattern, nickname);
		
		if (nickname.isBlank() || nickname.isEmpty() || 
				nickname.length() > MAX_NICKNAME_LENGTH
				|| !isRegexMatch) {
			result.put("result", "FORMAT_NOT_ALLOWED");
			return result;
		} 
		
		Member member = dao.getByNickname(nickname);
		if (member != null) {
			result.put("result", "NICKNAME_ALREADY_USED");
			return result;
		}
		
		result.put("result", "NICKNAME_VALID");
		return result;
	}

    /**
     * 닉네임 변경 -- TODO : 변경할 수 없을 경우 클라이언트에게 응답 해주어야함
     * @param memberId
     * @param nickname
     * @return
     */
	@Transactional
	@Override
	public Member updateNickname(int memberId, String nickname) {

        //멤버의 아이디를 통해 닉네임 로그 중 가장 최근 기록 하나를 불러옵니다.
         NicknameLog nicknameLog = nicknameLogDao.getLatestByMemberId(memberId);
       
         //해당 기록의 날짜를 얻습니다.
        Date date = nicknameLog.getCreatedAt();

        //해당 기록의 날짜와 현재날짜의 차이를 비교합니다.
        LocalDate localDate = new java.sql.Date(date.getTime()).toLocalDate();
        LocalDate now =LocalDate.now();

        //Period 타입은 P1Y2M33D 같은형식을 반환.
        Period period = Period.between(localDate, now);

        //조건 처리결과 period.getmonths -> "월"의차이만 비교
        //ex) 2022-11월과 2023-11월비교 -> 0
        //따라서 year를 추가하여 연도도 조건에 추가
        int year = period.getYears();
        int month = period.getMonths();

         //날짜의 차이가 30일 이내일 경우
         if (year<0 || month<0){
            //변경할 수 없음을 클라이언트에게 알려주기
            System.out.println("30일 이내에는 변경 불가함");
         }else {

            //닉네임 유효성 확인, 닉네임 중복확인
            System.out.println(nickname+"을 확인하세요");
            Map<String, Object> result = checkNicknameValid(nickname);
            if(result.get("result").equals("NICKNAME_VALID"))
            System.out.println(result+"결과입니다");
            dao.updateNickname(memberId, nickname);
            System.out.println("닉네임업데이트를 실행합니다");
            nicknameLogDao.insert(new NicknameLog(memberId, nickname));
            System.out.println("닉네임이 업데이트 되었습니다.");
        }

		return null;
	}
}
