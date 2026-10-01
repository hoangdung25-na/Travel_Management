const KEYCLOAK_HOST = process.env.NEXT_PUBLIC_KEYCLOAK_URL ?? "http://localhost:8180";
const REALM = process.env.NEXT_PUBLIC_KEYCLOAK_REALM ?? "travel-realm";
const CLIENT_ID = process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID ?? "travel-client";

/**
 * Trả về URL để redirect trình duyệt sang Keycloak, khởi chạy luồng đăng nhập Google.
 * Sau khi user xác nhận, Keycloak sẽ redirect ngược lại `redirectUri` kèm `code`.
 * `redirectUri` PHẢI được khai báo trong Keycloak Admin Console > Client > Valid Redirect URIs.
 */
export function buildGoogleLoginUrl(redirectUri: string): string {
  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    response_type: "code",
    scope: "openid profile email",
    identity_provider: "google",
    redirect_uri: redirectUri,
  });
  return `${KEYCLOAK_HOST}/realms/${REALM}/protocol/openid-connect/auth?${params.toString()}`;
}
