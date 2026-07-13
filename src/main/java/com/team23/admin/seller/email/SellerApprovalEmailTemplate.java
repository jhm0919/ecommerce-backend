package com.team23.admin.seller.email;

/**
 * 판매자 입점 승인 이메일 HTML 템플릿.
 */
public final class SellerApprovalEmailTemplate {

    private SellerApprovalEmailTemplate() {}

    public static String build(
            String businessName,
            String managerName,
            String loginId,
            String rawPassword
    ) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; max-width: 600px;
                             margin: 0 auto; padding: 20px;">

                  <h2 style="color: #333;">입점 신청이 승인되었습니다 🎉</h2>

                  <p>안녕하세요, <strong>%s</strong> 담당자 <strong>%s</strong>님.</p>
                  <p>입점 신청이 승인되었습니다. 아래 임시 계정으로 로그인하세요.</p>

                  <table style="border-collapse: collapse; width: 100%%; margin: 20px 0;">
                    <tr style="background-color: #f5f5f5;">
                      <td style="padding: 12px; border: 1px solid #ddd;
                                 font-weight: bold; width: 30%%;">임시 아이디</td>
                      <td style="padding: 12px; border: 1px solid #ddd;
                                 font-family: monospace; font-size: 16px;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding: 12px; border: 1px solid #ddd;
                                 font-weight: bold;">임시 비밀번호</td>
                      <td style="padding: 12px; border: 1px solid #ddd;
                                 font-family: monospace; font-size: 16px;">%s</td>
                    </tr>
                  </table>

                  <div style="background-color: #fff3cd; padding: 15px;
                              border-radius: 4px; border-left: 4px solid #ffc107;">
                    <strong>⚠️ 보안 안내</strong><br>
                    로그인 후 즉시 비밀번호를 변경해주세요.<br>
                    임시 비밀번호는 본 이메일 확인 후 즉시 폐기 바랍니다.
                  </div>

                  <p style="margin-top: 30px; color: #666; font-size: 13px;">
                    본 이메일은 자동 발송됩니다. 문의사항은 고객센터로 연락주세요.
                  </p>

                </body>
                </html>
                """.formatted(businessName, managerName, loginId, rawPassword);
    }
}