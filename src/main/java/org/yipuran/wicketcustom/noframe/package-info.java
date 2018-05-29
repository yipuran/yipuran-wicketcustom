/**
 * 枠なしのモーダルウィンドウ component パッケージ.
 * <PRE>
 * NoFrameWindow : 枠なしサイズ指定、
 * FullWindow    : フルスクリーン
 *
 * かならず、public static void closeCurrent(final IPartialPageRequestHandler target) でＣＬＯＳＥするようにすること。
 * モーダルの入れ子は保証しない。
 *
 * モーダルのＣＳＳの設定例、
 *
 * div.noframe-modal div.w_content {
 *    border: 3px solid #00ff00;
 * 	  padding: 10px;
 * 	  border-radius: 10px;
 * 	  background-color: #f5deb3;
 * }
 * </PRE>
 */
package org.yipuran.wicketcustom.noframe;
