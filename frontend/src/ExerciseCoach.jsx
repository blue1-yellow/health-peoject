import { useEffect, useMemo, useRef, useState } from 'react';
import { Camera } from 'lucide-react';

const angle = (a, b, c) => {
  const ab = { x: a.x - b.x, y: a.y - b.y };
  const cb = { x: c.x - b.x, y: c.y - b.y };
  const dot = ab.x * cb.x + ab.y * cb.y;
  const length = Math.hypot(ab.x, ab.y) * Math.hypot(cb.x, cb.y);
  return length ? Math.acos(Math.min(1, Math.max(-1, dot / length))) * 180 / Math.PI : 180;
};

export default function ExerciseCoach() {
  const videoRef = useRef(null); const streamRef = useRef(null); const modelRef = useRef(null); const frameRef = useRef(null);
  const phaseRef = useRef('up'); const lastTipRef = useRef(''); const lastPenaltyRef = useRef(0);
  const [running, setRunning] = useState(false); const [loading, setLoading] = useState(false); const [error, setError] = useState('');
  const [count, setCount] = useState(0); const [score, setScore] = useState(100); const [tip, setTip] = useState('调整好摄像头，让全身出现在画面中'); const [phase, setPhase] = useState('up');

  const setTipThrottled = (next) => { if (lastTipRef.current !== next) { lastTipRef.current = next; setTip(next); } };
  const analyze = (result) => {
    const points = result?.landmarks?.[0];
    if (!points) { setTipThrottled('未检测到完整人体，请调整位置和光线'); return; }
    const [leftHip,leftKnee,leftAnkle,rightHip,rightKnee,rightAnkle] = [points[23],points[25],points[27],points[24],points[26],points[28]];
    if (![leftHip,leftKnee,leftAnkle,rightHip,rightKnee,rightAnkle].every(p => p && p.visibility > 0.45)) { setTipThrottled('请确保髋部、膝盖和脚踝都在画面内'); return; }
    const kneeAngle = (angle(leftHip,leftKnee,leftAnkle) + angle(rightHip,rightKnee,rightAnkle)) / 2;
    const kneeIn = leftKnee.x > leftAnkle.x + 0.08 || rightKnee.x < rightAnkle.x - 0.08;
    const now = performance.now();
    if (kneeIn) {
      setTipThrottled('请保持膝盖朝向脚尖，避免膝盖内扣');
      if (now - lastPenaltyRef.current > 700) { lastPenaltyRef.current = now; setScore(value => Math.max(60, value - 1)); }
    } else if (kneeAngle < 105) setTipThrottled('下蹲幅度不错，准备起身');
    else setTipThrottled('慢慢下蹲，保持动作稳定');
    if (kneeAngle < 105 && phaseRef.current === 'up') { phaseRef.current = 'down'; setPhase('down'); }
    if (kneeAngle > 155 && phaseRef.current === 'down') { phaseRef.current = 'up'; setPhase('up'); setCount(value => value + 1); }
  };

  const detect = () => {
    if (!modelRef.current || !videoRef.current || videoRef.current.readyState < 2) { frameRef.current = requestAnimationFrame(detect); return; }
    try { analyze(modelRef.current.detectForVideo(videoRef.current, performance.now())); }
    catch { setTipThrottled('正在重新识别，请保持全身在画面内'); }
    frameRef.current = requestAnimationFrame(detect);
  };

  const stop = () => {
    setRunning(false);
    if (frameRef.current) cancelAnimationFrame(frameRef.current);
    streamRef.current?.getTracks().forEach(track => track.stop()); streamRef.current = null;
    if (videoRef.current) videoRef.current.srcObject = null;
    modelRef.current?.close(); modelRef.current = null;
  };

  const start = async () => {
    setError(''); setLoading(true);
    try {
      if (!navigator.mediaDevices?.getUserMedia) throw new Error('当前页面无法访问摄像头，请使用 localhost 或 HTTPS 打开');
      const vision = await import('https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@0.10.35/vision_bundle.mjs');
      const fileset = await vision.FilesetResolver.forVisionTasks('https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@0.10.35/wasm');
      modelRef.current = await vision.PoseLandmarker.createFromOptions(fileset, {
        baseOptions: { modelAssetPath: 'https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task', delegate: 'GPU' },
        runningMode: 'VIDEO', numPoses: 1,
      });
      streamRef.current = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user', width: { ideal: 640 }, height: { ideal: 480 } }, audio: false });
      videoRef.current.srcObject = streamRef.current; await videoRef.current.play();
      phaseRef.current = 'up'; lastTipRef.current = ''; lastPenaltyRef.current = 0;
      setCount(0); setScore(100); setPhase('up'); setTip('准备开始，缓慢完成深蹲'); setRunning(true);
    } catch (e) {
      modelRef.current?.close(); modelRef.current = null;
      streamRef.current?.getTracks().forEach(track => track.stop()); streamRef.current = null;
      setError(e.name === 'NotAllowedError' ? '请允许浏览器使用摄像头后重试。' : `启动失败：${e.message}`);
    } finally { setLoading(false); }
  };

  useEffect(() => { if (running) frameRef.current = requestAnimationFrame(detect); return () => { if (frameRef.current) cancelAnimationFrame(frameRef.current); }; }, [running]);
  useEffect(() => () => { streamRef.current?.getTracks().forEach(track => track.stop()); modelRef.current?.close(); }, []);

  return <>
    <div className="page-title"><div><p className="eyebrow">MOVEMENT COACH</p><h1>深蹲动作指导</h1><p>浏览器本地识别姿态，不上传或保存摄像头视频。</p></div></div>
    <section className="coach-layout">
      <div className="coach-video panel"><div className="video-stage">
        {!running && <div className="video-placeholder"><Camera size={34}/><strong>准备开始训练</strong><span>请将摄像头放在身体侧前方，保证全身入镜</span></div>}
        <video ref={videoRef} muted playsInline className={running ? 'video-live' : 'video-hidden'}/>
        {running && <span className="live-badge"><i/>实时识别</span>}
      </div><div className="coach-actions">{!running ? <button className="primary" onClick={start} disabled={loading}><Camera size={17}/>{loading ? '加载识别模型...' : '开始实时指导'}</button> : <button className="outlined" onClick={stop}>结束训练</button>}<span>摄像头画面仅在本地处理；结束训练会关闭摄像头。</span></div>{error && <p className="notice">{error}</p>}</div>
      <div className="coach-side"><div className="coach-stat"><span>完成次数</span><strong>{count}</strong><small>次</small></div><div className="coach-stat"><span>动作评分</span><strong>{Math.round(score)}</strong><small>/ 100</small></div><div className="coach-tip"><p className="eyebrow">实时提示</p><h2>{tip}</h2><div className="phase-pill">{phase === 'down' ? '下蹲中' : '站立准备'}</div></div></div>
    </section>
    <section className="panel coach-guide"><p className="eyebrow">HOW IT WORKS</p><h2>开始前请注意安全</h2><div className="guide-steps"><span><b>01</b>允许摄像头权限</span><span><b>02</b>让全身保持在画面内</span><span><b>03</b>根据提示缓慢调整</span></div><p className="coach-disclaimer">动作识别仅提供一般性运动提示，受摄像头角度、光线和遮挡影响，不替代专业教练或医疗建议。如感到疼痛或不适，请立即停止。</p></section>
  </>;
}
