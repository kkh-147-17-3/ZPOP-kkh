import state from "./state.js";

/**
 * 페이징 요청 url을 리턴
 * @param {boolean} isScrollEvent 스크롤 이벤트 여부
 */
export function generateUrl(isScrollEvent) {
  const { isToggleOn, searchKeyword, regions, category } = state;
  const parameter = {};
  let lastMeetingId;

  // 토글 checked === true -> 모집 중인 모임만 보기
  if (isToggleOn) parameter.isClosed = false;

  // 사용자가 검색한 검색어가 있을경우
  if (searchKeyword) parameter.keyword = searchKeyword;

  // 지역 옵션이 선택되었다면
  if (state.regions.length !== 0) parameter.regions = regions;

  // 특정 카테고리가 선택되었다면
  if (category && category !== 0) parameter.category = category;

  // 무한 스크롤 이벤트라면
  if (isScrollEvent) {
    lastMeetingId =
      document.querySelector("#meetings").lastElementChild.dataset.id;
    parameter.start = lastMeetingId;
  }
  
  const searchParams = new URLSearchParams(parameter).toString();

  return `/meeting/api/list?${searchParams}`;
}

