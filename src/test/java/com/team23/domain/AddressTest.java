package com.team23.domain;

import com.team23.order.delivery.domain.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AddressTest {

    @Nested
    @DisplayName("생성")
    class Creation {

        @Test
        @DisplayName("정상적인 주소를 생성할 수 있다")
        void createValidAddress() {
            Address address = new Address(
                    "06236",
                    "서울시 강남구 테헤란로 152",
                    "강남파이낸스센터 10층"
            );

            assertThat(address.getZipCode()).isEqualTo("06236");
            assertThat(address.getAddressLine1()).isEqualTo("서울시 강남구 테헤란로 152");
            assertThat(address.getAddressLine2()).isEqualTo("강남파이낸스센터 10층");
        }

        @Test
        @DisplayName("상세 주소(addressLine2)가 null이어도 생성 가능하다")
        void createWithoutDetailAddress() {
            Address address = new Address(
                    "06236",
                    "서울시 강남구 테헤란로 152",
                    null
            );

            assertThat(address.getAddressLine2()).isNull();
            assertThat(address.hasDetailAddress()).isFalse();
        }

        @Test
        @DisplayName("앞뒤 공백은 자동으로 제거된다")
        void trimWhitespace() {
            Address address = new Address(
                    "  06236  ",
                    "  서울시 강남구  ",
                    "  10층  "
            );

            assertThat(address.getZipCode()).isEqualTo("06236");
            assertThat(address.getAddressLine1()).isEqualTo("서울시 강남구");
            assertThat(address.getAddressLine2()).isEqualTo("10층");
        }
    }

    @Nested
    @DisplayName("필수값 검증")
    class RequiredFieldValidation {

        @Test
        @DisplayName("zipCode가 null이면 예외")
        void rejectNullZipCode() {
            assertThatThrownBy(() -> new Address(null, "주소", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("zipCode");
        }

        @Test
        @DisplayName("zipCode가 빈 문자열이면 예외")
        void rejectBlankZipCode() {
            assertThatThrownBy(() -> new Address("", "주소", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blank");

            assertThatThrownBy(() -> new Address("   ", "주소", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blank");
        }

        @Test
        @DisplayName("addressLine1이 null이면 예외")
        void rejectNullAddressLine1() {
            assertThatThrownBy(() -> new Address("06236", null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("addressLine1");
        }

        @Test
        @DisplayName("addressLine1이 빈 문자열이면 예외")
        void rejectBlankAddressLine1() {
            assertThatThrownBy(() -> new Address("06236", "", null))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> new Address("06236", "   ", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("길이 제한")
    class LengthValidation {

        @Test
        @DisplayName("zipCode가 10자를 초과하면 예외")
        void rejectTooLongZipCode() {
            String tooLong = "1".repeat(11);

            assertThatThrownBy(() -> new Address(tooLong, "주소", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exceed");
        }

        @Test
        @DisplayName("addressLine1이 200자를 초과하면 예외")
        void rejectTooLongAddressLine1() {
            String tooLong = "가".repeat(201);

            assertThatThrownBy(() -> new Address("06236", tooLong, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("addressLine2가 200자를 초과하면 예외")
        void rejectTooLongAddressLine2() {
            String tooLong = "가".repeat(201);

            assertThatThrownBy(() -> new Address("06236", "주소", tooLong))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("편의 메서드")
    class ConvenienceMethods {

        @Test
        @DisplayName("hasDetailAddress: 상세주소가 있으면 true")
        void hasDetailAddressTrue() {
            Address address = new Address("06236", "주소", "10층");

            assertThat(address.hasDetailAddress()).isTrue();
        }

        @Test
        @DisplayName("hasDetailAddress: 상세주소가 null이면 false")
        void hasDetailAddressFalseForNull() {
            Address address = new Address("06236", "주소", null);

            assertThat(address.hasDetailAddress()).isFalse();
        }

        @Test
        @DisplayName("getFullAddress: 전체 주소를 한 줄로 합친다")
        void getFullAddressWithDetail() {
            Address address = new Address(
                    "06236",
                    "서울시 강남구 테헤란로 152",
                    "강남파이낸스센터 10층"
            );

            assertThat(address.getFullAddress())
                    .isEqualTo("06236 서울시 강남구 테헤란로 152 강남파이낸스센터 10층");
        }

        @Test
        @DisplayName("getFullAddress: 상세주소 없으면 우편번호 + 기본주소만")
        void getFullAddressWithoutDetail() {
            Address address = new Address("06236", "서울시 강남구 테헤란로 152", null);

            assertThat(address.getFullAddress())
                    .isEqualTo("06236 서울시 강남구 테헤란로 152");
        }
    }

    @Nested
    @DisplayName("값 동등성")
    class ValueEquality {

        @Test
        @DisplayName("모든 필드가 같으면 동등하다")
        void equalForSameFields() {
            Address a = new Address("06236", "서울시", "10층");
            Address b = new Address("06236", "서울시", "10층");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("addressLine2가 모두 null이어도 동등하다")
        void equalWithBothNullDetail() {
            Address a = new Address("06236", "서울시", null);
            Address b = new Address("06236", "서울시", null);

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("우편번호가 다르면 다르다")
        void notEqualForDifferentZipCode() {
            Address a = new Address("06236", "서울시", null);
            Address b = new Address("12345", "서울시", null);

            assertThat(a).isNotEqualTo(b);
        }
    }
}