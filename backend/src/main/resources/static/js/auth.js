/**
 * パスワード表示切り替え機能
 * @param {string} inputId - パスワード入力欄のID
 * @param {string} buttonId - 切り替えボタンのID
 * @param {string} iconId - アイコンのID
 */
function setupPasswordToggle(inputId, buttonId, iconId) {
    const input = document.getElementById(inputId);
    const button = document.getElementById(buttonId);
    const icon = document.getElementById(iconId);

    // そのページに要素が存在する場合のみイベントを設定
    if (input && button && icon) {
        button.addEventListener('click', function () {
            const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
            input.setAttribute('type', type);

            if (type === 'password') {
                icon.classList.remove('bi-eye');
                icon.classList.add('bi-eye-slash');
            } else {
                icon.classList.remove('bi-eye-slash');
                icon.classList.add('bi-eye');
            }
        });
    }
}

// 画面読み込み完了時に実行
document.addEventListener('DOMContentLoaded', function() {
    // ログイン・登録画面共通（メインパスワード）
    setupPasswordToggle('password', 'togglePassword', 'toggleIcon');

    // 登録画面のみ（確認用パスワード）
    setupPasswordToggle('confirmPassword', 'toggleConfirmPassword', 'toggleConfirmIcon');
});