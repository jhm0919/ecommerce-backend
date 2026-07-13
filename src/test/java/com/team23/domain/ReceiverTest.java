package com.team23.domain;

import com.team23.order.delivery.domain.Receiver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ReceiverTest {

    @Nested
    @DisplayName("생성")
    class Creation {

        @Test
        @DisplayName("정상적인 수령인을 생성할 수 있다")
        void createValidReceiver() {
            Receiver receiver = new Receiver("홍길동", "010-1234-5678");

            assertThat(receiver.getName()).isEqualTo("홍길동");
            assertThat(receiver.getPhone()).isEqualTo("01012345678");
        }

        @Test
        @DisplayName("전화번호 하이픈은 제거된다")
        void normalizePhoneRemovesHyphens() {
            Receiver receiver = new Receiver("홍길동", "010-1234-5678");

            assertThat(receiver.getPhone()).isEqualTo("01012345678");
        }

        @Test
        @DisplayName("전화번호 공백은 제거된다")
        void normalizePhoneRemovesSpaces() {
            Receiver receiver = new Receiver("홍길동", "010 1234 5678");

            assertThat(receiver.getPhone()).isEqualTo("01012345678");
        }

        @Test
        @DisplayName("국제전화 형식의 + 는 유지된다")
        void preservePlusInPhone() {
            Receiver receiver = new Receiver("John Doe", "+82-10-1234-5678");

            assertThat(receiver.getPhone()).isEqualTo("+821012345678");
        }

        @Test
        @DisplayName("이름의 앞뒤 공백은 제거된다")
        void trimName() {
            Receiver receiver = new Receiver("  홍길동  ", "01012345678");

            assertThat(receiver.getName()).isEqualTo("홍길동");
        }
    }

    @Nested
    @DisplayName("이름 검증")
    class NameValidation {

        @Test
        @DisplayName("이름이 null이면 예외")
        void rejectNullName() {
            assertThatThrownBy(() -> new Receiver(null, "01012345678"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("이름이 빈 문자열이면 예외")
        void rejectBlankName() {
            assertThatThrownBy(() -> new Receiver("", "01012345678"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blank");

            assertThatThrownBy(() -> new Receiver("   ", "01012345678"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("이름이 50자를 초과하면 예외")
        void rejectTooLongName() {
            String tooLong = "가".repeat(51);

            assertThatThrownBy(() -> new Receiver(tooLong, "01012345678"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exceed");
        }
    }

    @Nested
    @DisplayName("전화번호 검증")
    class PhoneValidation {

        @Test
        @DisplayName("전화번호가 null이면 예외")
        void rejectNullPhone() {
            assertThatThrownBy(() -> new Receiver("홍길동", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("phone");
        }

        @Test
        @DisplayName("전화번호에 숫자가 하나도 없으면 예외")
        void rejectPhoneWithoutDigits() {
            assertThatThrownBy(() -> new Receiver("홍길동", "abc"))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> new Receiver("홍길동", "---"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("전화번호 숫자가 9자 미만이면 예외")
        void rejectTooShortPhone() {
            assertThatThrownBy(() -> new Receiver("홍길동", "12345678"))  // 8자
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least");
        }

        @Test
        @DisplayName("전화번호 숫자가 20자를 초과하면 예외")
        void rejectTooLongPhone() {
            String tooLong = "1".repeat(21);

            assertThatThrownBy(() -> new Receiver("홍길동", tooLong))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("마스킹")
    class Masking {

        @Test
        @DisplayName("이름 마스킹: 3글자 이상은 가운데 마스킹")
        void maskNameForLongName() {
            Receiver receiver = new Receiver("홍길동", "01012345678");

            assertThat(receiver.getMaskedName()).isEqualTo("홍*동");
        }

        @Test
        @DisplayName("이름 마스킹: 4글자도 가운데 마스킹")
        void maskNameForFourCharacters() {
            Receiver receiver = new Receiver("남궁민수", "01012345678");

            assertThat(receiver.getMaskedName()).isEqualTo("남**수");
        }

        @Test
        @DisplayName("이름 마스킹: 2글자는 마지막 글자만 마스킹")
        void maskNameForTwoCharacters() {
            Receiver receiver = new Receiver("홍씨", "01012345678");

            assertThat(receiver.getMaskedName()).isEqualTo("홍*");
        }

        @Test
        @DisplayName("전화번호 마스킹: 한국 휴대전화 형식")
        void maskKoreanMobilePhone() {
            Receiver receiver = new Receiver("홍길동", "010-1234-5678");

            assertThat(receiver.getMaskedPhone()).isEqualTo("010-****-5678");
        }
    }

    @Nested
    @DisplayName("값 동등성")
    class ValueEquality {

        @Test
        @DisplayName("이름과 전화번호가 같으면 동등하다")
        void equalForSameFields() {
            Receiver a = new Receiver("홍길동", "010-1234-5678");
            Receiver b = new Receiver("홍길동", "01012345678");  // 같은 번호 다른 형식

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("이름이 다르면 다르다")
        void notEqualForDifferentName() {
            Receiver a = new Receiver("홍길동", "01012345678");
            Receiver b = new Receiver("김철수", "01012345678");

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("전화번호가 다르면 다르다")
        void notEqualForDifferentPhone() {
            Receiver a = new Receiver("홍길동", "01012345678");
            Receiver b = new Receiver("홍길동", "01087654321");

            assertThat(a).isNotEqualTo(b);
        }
    }
}