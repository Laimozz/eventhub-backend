#!/bin/sh
# EventHub Automation Test CLI argument resolver for POSIX sh / Git Bash
MVN_BIN="$1"
shift

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
TESTS_ROOT="$PROJECT_ROOT/src/test/java/com/eventhub/automation/tests"

uc_label() {
  case "$1" in
    01) echo "Dang ky tai khoan (Register)" ;;
    02) echo "Dang nhap he thong (Login)" ;;
    03) echo "Dang xuat (Logout)" ;;
    04) echo "Quan ly thong tin ca nhan (Profile)" ;;
    05) echo "Tim kiem su kien (Search)" ;;
    06) echo "Loc su kien da tieu chi (Filter)" ;;
    07) echo "Dat ve & Thanh toan (Booking)" ;;
    08) echo "Lich su & Chi tiet Booking" ;;
    09) echo "Ve dien tu & QR Code (My Tickets)" ;;
    10) echo "Thong bao nguoi dung (Notifications)" ;;
    11) echo "Chat voi AI (AI Assistant Chatbot)" ;;
    12) echo "Ban to chuc tao su kien (Create Event)" ;;
    13) echo "Phan cong nhan vien (Staff Dispatch)" ;;
    14) echo "Huy su kien (Cancel Event)" ;;
    15) echo "Yeu cau rut tien (Organizer Wallet)" ;;
    16) echo "Soat ve & Check-in QR (Staff)" ;;
    17) echo "Admin quan ly nguoi dung (Users)" ;;
    18) echo "Quan ly danh muc su kien (Categories)" ;;
    19) echo "Phe duyet su kien (Event Approval)" ;;
    20) echo "Hoan tien su kien huy (Refunds)" ;;
    21) echo "Phe duyet rut tien (Admin Payout)" ;;
    22) echo "Bao cao ban to chuc (Organizer Reports)" ;;
    23) echo "Chinh sua su kien (Edit Event)" ;;
    24) echo "Dashboard & Thong ke Admin" ;;
    25) echo "Doi mat khau (Change Password)" ;;
    *) echo "" ;;
  esac
}

show_usage() {
  echo ""
  echo "=== EventHub Automation Test CLI ==="
  echo "Cu phap: ./mvnw test <muc-tieu> [tuy-chon]"
  echo ""
  echo "Vi du nhanh:"
  echo "  ./mvnw test UC01                   Chay toan bo test cua UC01"
  echo "  ./mvnw test UC01 UC02              Chay nhieu UC (UC01 va UC02)"
  echo "  ./mvnw test UC01-UC05              Chay theo dai UC (UC01 den UC05)"
  echo "  ./mvnw test UC01_01                Chay 1 test case cu the (TC_UC01_01*)"
  echo "  ./mvnw test customer               Chay toan bo UC cua Customer (UC01-UC10)"
  echo "  ./mvnw test organizer              Chay toan bo UC cua Organizer (UC12-UC15,UC22,UC23)"
  echo "  ./mvnw test staff                  Chay toan bo UC cua Staff (UC16)"
  echo "  ./mvnw test admin                  Chay toan bo UC cua Admin (UC17-UC21,UC24)"
  echo "  ./mvnw test smoke                  Chay CheckEnvironment + test _01 cua moi UC"
  echo "  ./mvnw test env                    Chay CheckEnvironmentTest"
  echo "  ./mvnw test all                    Chay full suite (testng.xml)"
  echo ""
  echo "Tuy chon: headless | fast | slow | chrome | edge | no-rollback | ignore-fail"
  echo "Vi du:    ./mvnw test UC01 headless fast"
  echo ""
  total=0
  for code in 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25; do
    cnt=$(find "$TESTS_ROOT" -maxdepth 2 -type f -name "TC_UC${code}_*.java" 2>/dev/null | wc -l | tr -d ' ')
    total=$((total + cnt))
    printf "  UC%s  (%2s tests)  - %s\n" "$code" "$cnt" "$(uc_label "$code")"
  done
  echo ""
  echo "Tong cong: $total Test Cases."
  echo ""
}

pad2() {
  case "$1" in
    [0-9]) echo "0$1" ;;
    *) echo "$1" ;;
  esac
}

GOALS=""
FLAGS=""
PATTERNS=""
HAS_DTEST=0
FORCE_ALL=0

append_pattern() {
  case ",$PATTERNS," in
    *,"$1",*) ;;
    *) PATTERNS="${PATTERNS:+$PATTERNS,}$1" ;;
  esac
}

add_uc() {
  padded=$(pad2 "$1")
  if [ -z "$(uc_label "$padded")" ]; then
    echo "[EventHub Test] Khong tim thay UC$padded. Go './mvnw test list' de xem danh sach." >&2
    return 1
  fi
  append_pattern "TC_UC${padded}_*"
  return 0
}

for raw in "$@"; do
  [ -n "$raw" ] || continue
  lower=$(printf '%s' "$raw" | tr '[:upper:]' '[:lower:]')

  case "$lower" in
    clean|test) GOALS="$GOALS $lower"; continue ;;
    list|help|--help|-h|--list) show_usage; exit 0 ;;
    headless|--headless|-headless) FLAGS="$FLAGS -Dheadless=true"; continue ;;
    ui|headed|--ui|--headed) FLAGS="$FLAGS -Dheadless=false"; continue ;;
    fast|--fast) FLAGS="$FLAGS -Dstep.delay=0"; continue ;;
    normal|--normal) FLAGS="$FLAGS -Dstep.delay=500"; continue ;;
    slow|--slow) FLAGS="$FLAGS -Dstep.delay=2000"; continue ;;
    chrome) FLAGS="$FLAGS -Dbrowser=chrome"; continue ;;
    edge) FLAGS="$FLAGS -Dbrowser=edge"; continue ;;
    no-rollback|--no-rollback) FLAGS="$FLAGS -Ddb.rollback.enabled=false"; continue ;;
    ignore-fail|--ignore-fail) FLAGS="$FLAGS -Dtest.failure.ignore=true"; continue ;;
    all) FORCE_ALL=1; continue ;;
    env|check) append_pattern "CheckEnvironmentTest"; continue ;;
    smoke) append_pattern "CheckEnvironmentTest"; append_pattern "TC_UC*_01_*"; continue ;;
    customer)
      for n in 01 02 03 04 05 06 07 08 09 10 11 25; do add_uc "$n"; done
      continue
      ;;
    ai)
      add_uc "11"
      continue
      ;;
    organizer)
      for n in 12 13 14 15 22 23; do add_uc "$n"; done
      continue
      ;;
    staff)
      add_uc "16"
      continue
      ;;
    admin)
      for n in 17 18 19 20 21 24; do add_uc "$n"; done
      continue
      ;;
  esac

  case "$raw" in
    -Dtest=*) HAS_DTEST=1; FLAGS="$FLAGS $raw"; continue ;;
    -*) FLAGS="$FLAGS $raw"; continue ;;
  esac

  OLD_IFS="$IFS"
  IFS=','
  for token in $raw; do
    IFS="$OLD_IFS"
    tlower=$(printf '%s' "$token" | tr '[:upper:]' '[:lower:]')
    if printf '%s' "$tlower" | grep -Eq '^uc[0-9]{1,2}-(uc)?[0-9]{1,2}$'; then
      s=$(printf '%s' "$tlower" | sed -E 's/^uc([0-9]{1,2})-.*/\1/')
      e=$(printf '%s' "$tlower" | sed -E 's/^uc[0-9]{1,2}-(uc)?([0-9]{1,2})$/\2/')
      s=$((10#$s)); e=$((10#$e))
      if [ "$s" -gt "$e" ]; then tmp="$s"; s="$e"; e="$tmp"; fi
      i="$s"
      while [ "$i" -le "$e" ]; do
        add_uc "$i" || exit 1
        i=$((i + 1))
      done
    elif printf '%s' "$tlower" | grep -Eq '^uc[0-9]{1,2}$'; then
      n=$(printf '%s' "$tlower" | sed -E 's/^uc([0-9]{1,2})$/\1/')
      add_uc "$n" || exit 1
    elif printf '%s' "$tlower" | grep -Eq '^(tc_)?uc[0-9]{1,2}_[0-9]{1,2}$'; then
      u=$(printf '%s' "$tlower" | sed -E 's/^(tc_)?uc([0-9]{1,2})_.*/\2/')
      c=$(printf '%s' "$tlower" | sed -E 's/^(tc_)?uc[0-9]{1,2}_([0-9]{1,2})$/\2/')
      up=$(pad2 "$u"); cp=$(pad2 "$c")
      append_pattern "TC_UC${up}_${cp}*"
    elif printf '%s' "$token" | grep -Eq '^(TC_UC|.*Test$|.*\*).*'; then
      append_pattern "$token"
    else
      echo "[EventHub Test] Khong hieu tham so: '$token'. Go './mvnw test list' de xem huong dan." >&2
      exit 1
    fi
  done
  IFS="$OLD_IFS"
done

[ -n "$GOALS" ] || GOALS="test"

if [ "$HAS_DTEST" -eq 0 ] && [ "$FORCE_ALL" -eq 0 ] && [ -n "$PATTERNS" ]; then
  echo "[EventHub Test] Target: $PATTERNS"
  # shellcheck disable=SC2086
  exec "$MVN_BIN" $GOALS "-Dtest=$PATTERNS" $FLAGS
else
  # shellcheck disable=SC2086
  exec "$MVN_BIN" $GOALS $FLAGS
fi

