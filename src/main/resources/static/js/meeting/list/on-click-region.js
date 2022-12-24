import state from "./state.js";

export default function (e) {
  const MAX_REGION_COUNT = 3;

  if (state.regions.length >= MAX_REGION_COUNT) return;

  const regionId = e.target.dataset.id;

  // 이미 선택한 지역이라면 지우기
  if (state.regions.find((r) => r === regionId)) {
    const selectedRegion = document.querySelector(
      `[data-region-id="${regionId}"]`
    );
    selectedRegion.remove();
    removeRegionInState(regionId);
    // TODO: 재요청
    return;
  }

  // view에 선택한 지역 생성
  const region = e.target.innerText;
  const template = `
		<div class="meeting-region" data-region-id=${regionId}>
				<span>${region}</span>
				<img id="meeting-region__close" src="/images/icon/x-white.svg">
		</div>`;
  const meetingActivateOption = document.querySelector(
    ".meeting-activate-option"
  );
  meetingActivateOption.insertAdjacentHTML("beforebegin", template);

  // state에 region id 넣기
  state.regions.push(regionId);

  // X 버튼 이벤트 추가
  const newRegion = document.querySelector(`[data-region-id="${regionId}"]`);
  const closeBtn = newRegion.querySelector("img");
  closeBtn.onclick = removeRegion;

  // TODO: 재요청

  function removeRegion(e) {
    const selectedRegion = e.target.parentElement;
    const regionId = selectedRegion.dataset.regionId;
    document.querySelector(".meeting__options").removeChild(selectedRegion);
    removeRegionInState(regionId);
    // TODO: 재요청
  }

  /**
   * state에 있는 region id 제거
   */
  function removeRegionInState(regionId) {
    state.regions = state.regions.filter((r) => r !== regionId);
  }
}
