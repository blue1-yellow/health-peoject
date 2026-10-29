import { useEffect, useMemo, useState } from 'react';
import {
  Activity, Apple, ArrowUpRight, Bot, CalendarDays, Camera, ChevronRight, CircleUserRound,
  Dumbbell, FileText, HeartPulse, LayoutDashboard, LogOut, MessageCircle, Moon, Plus,
  Send, Sparkles, Utensils, X,
} from 'lucide-react';
import { api } from './api';
import ExerciseCoach from './ExerciseCoach';

const now = () => new Date().toISOString().slice(0, 16);
const navItems = [
  ['dashboard', '健康概览', LayoutDashboard], ['food', '饮食记录', Utensils],
  ['coach', '动作指导', Camera], ['ai', 'AI 健康助手', Bot], ['sport', '运动记录', Dumbbell], ['mood', '情绪记录', HeartPulse],
];

function formatTime(value) {
  if (!value) return '--';
  return new Intl.DateTimeFormat('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(value));
}

function beijingNow() {
  const parts = new Intl.DateTimeFormat('zh-CN', {
    timeZone: 'Asia/Shanghai',
    hour: 'numeric', hour12: false,
    year: 'numeric', month: 'numeric', day: 'numeric', weekday: 'long',
  }).formatToParts(new Date());
  return Object.fromEntries(parts.filter(part => part.type !== 'literal').map(part => [part.type, part.value]));
}

function greetingByBeijingTime() {
  const hour = Number(beijingNow().hour);
  if (hour >= 5 && hour < 11) return '早上好';
  if (hour >= 11 && hour < 13) return '中午好';
  if (hour >= 13 && hour < 18) return '下午好';
  return '晚上好';
}

function Auth({ onSuccess }) {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ username: '', password: '', name: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const submit = async (event) => {
    event.preventDefault(); setLoading(true); setError('');
    try {
      if (mode === 'register') { await api.register(form); setMode('login'); setError('注册成功，请登录'); }
      else { const token = await api.login(form); localStorage.setItem('health_token', token); localStorage.setItem('health_user', form.username); onSuccess(); }
    } catch (err) { setError(err.message); } finally { setLoading(false); }
  };
  return <main className="auth-page">
    <section className="auth-visual">
      <div className="brand-mark"><Activity size={24} /></div>
      <p className="eyebrow light">HEALTH COMPANION</p>
      <h1>把每一天，<br />过得更有觉察。</h1>
      <p>记录饮食、运动和情绪，让健康习惯成为看得见的日常。</p>
      <div className="auth-image" aria-label="健康生活场景" />
    </section>
    <section className="auth-card-wrap">
      <form className="auth-card" onSubmit={submit}>
        <div className="auth-card-heading"><p className="eyebrow">WELCOME</p><h2>{mode === 'login' ? '欢迎回来' : '创建你的账号'}</h2><p>{mode === 'login' ? '登录后继续你的健康旅程' : '用几秒钟，开启健康记录'}</p></div>
        {mode === 'register' && <label>昵称<input required value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="例如：小安" /></label>}
        <label>用户名<input required value={form.username} onChange={e => setForm({ ...form, username: e.target.value })} placeholder="请输入用户名" /></label>
        <label>密码<input required type="password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} placeholder="请输入密码" /></label>
        {error && <p className={error.includes('成功') ? 'notice success' : 'notice'}>{error}</p>}
        <button className="primary wide" disabled={loading}>{loading ? '处理中...' : mode === 'login' ? '登录' : '注册账号'}<ArrowUpRight size={17} /></button>
        <button type="button" className="text-button" onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}>{mode === 'login' ? '还没有账号？去注册' : '已有账号？去登录'}</button>
      </form>
    </section>
  </main>;
}

function Modal({ title, children, onClose }) { return <div className="modal-backdrop"><div className="modal"><button className="icon-button modal-close" onClick={onClose} title="关闭"><X size={19} /></button><h2>{title}</h2>{children}</div></div>; }

function RecordForm({ type, onClose, onSaved }) {
  const [form, setForm] = useState(type === 'food'
    ? { foodName: '', cal: '', note: '', eatTime: now() }
    : type === 'sport' ? { sportName: '', duration: '', calorie: '', remark: '', recordTime: now() }
      : { moodScore: '7', moodDesc: '', remark: '', recordTime: now() });
  const [error, setError] = useState('');
  const [recognizing, setRecognizing] = useState(false);
  const recognizeFood = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    setRecognizing(true); setError('');
    try {
      const result = await api.identifyFood(file);
      const parsed = JSON.parse(result.reply);
      setForm(current => ({ ...current, foodName: parsed.foodName || current.foodName, cal: parsed.calories || current.cal, note: (current.note ? current.note + '；' : '') + (parsed.note || 'AI 图片识别结果，请确认热量') }));
    } catch (err) { setError(err.message || '图片识别失败'); }
    finally { setRecognizing(false); event.target.value = ''; }
  };
  const submit = async (e) => { e.preventDefault(); try {
    const payload = { ...form };
    if (type === 'food') { payload.cal = Number(payload.cal); payload.eatTime = `${payload.eatTime}:00`; await api.addFood(payload); }
    if (type === 'sport') { payload.duration = Number(payload.duration); payload.calorie = Number(payload.calorie); await api.addSport(payload); }
    if (type === 'mood') { payload.moodScore = Number(payload.moodScore); await api.addMood(payload); }
    await onSaved(); onClose();
  } catch (err) { setError(err.message); } };
  const fields = type === 'food' ? <><label>食物名称<input required value={form.foodName} onChange={e => setForm({...form, foodName:e.target.value})} placeholder="例如：燕麦酸奶碗" /></label><label>热量（kcal）<input required type="number" min="0" value={form.cal} onChange={e => setForm({...form, cal:e.target.value})} /></label><label>用餐时间<input required type="datetime-local" value={form.eatTime} onChange={e => setForm({...form, eatTime:e.target.value})} /></label><label>备注<input value={form.note} onChange={e => setForm({...form, note:e.target.value})} placeholder="可选" /></label></> : type === 'sport' ? <><label>运动类型<input required value={form.sportName} onChange={e => setForm({...form, sportName:e.target.value})} placeholder="例如：慢跑" /></label><div className="form-split"><label>时长（分钟）<input required type="number" min="1" value={form.duration} onChange={e => setForm({...form, duration:e.target.value})} /></label><label>消耗热量（kcal）<input required type="number" min="0" value={form.calorie} onChange={e => setForm({...form, calorie:e.target.value})} /></label></div><label>运动时间<input required type="datetime-local" value={form.recordTime} onChange={e => setForm({...form, recordTime:e.target.value})} /></label><label>备注<input value={form.remark} onChange={e => setForm({...form, remark:e.target.value})} placeholder="可选" /></label></> : <><label>此刻感受<input required value={form.moodDesc} onChange={e => setForm({...form, moodDesc:e.target.value})} placeholder="例如：平静、愉悦" /></label><label>情绪评分 <span className="score-value">{form.moodScore}/10</span><input type="range" min="1" max="10" value={form.moodScore} onChange={e => setForm({...form, moodScore:e.target.value})} /></label><label>记录时间<input required type="datetime-local" value={form.recordTime} onChange={e => setForm({...form, recordTime:e.target.value})} /></label><label>备注<input value={form.remark} onChange={e => setForm({...form, remark:e.target.value})} placeholder="此刻的想法..." /></label></>;
  return <Modal title={type === 'food' ? '添加饮食记录' : type === 'sport' ? '添加运动记录' : '记录此刻心情'} onClose={onClose}><form className="record-form" onSubmit={submit}>{type === 'food' && <label className="image-upload">食物图片识别<input type="file" accept="image/*" onChange={recognizeFood} disabled={recognizing} />{recognizing && <small>正在识别图片中的食物与热量...</small>}</label>}{fields}{error && <p className="notice">{error}</p>}<button className="primary wide">保存记录 <ArrowUpRight size={17} /></button></form></Modal>;
}

function Dashboard({ data, onAdd, setView }) {
  const calories = data.food.reduce((total, item) => total + (Number(item.cal) || 0), 0);
  const duration = data.sport.reduce((total, item) => total + (Number(item.duration) || 0), 0);
  const mood = data.mood.at(-1)?.moodScore ?? '--';
  const today = beijingNow();
  return <><div className="page-title"><div><p className="eyebrow">TODAY · {today.month}月{today.day}日 {today.weekday}</p><h1>{greetingByBeijingTime()}，{localStorage.getItem('health_user') || '朋友'}。</h1><p>从一个小记录开始，照顾今天的自己。</p></div><button className="primary" onClick={() => onAdd('mood')}><Plus size={17} />记录此刻</button></div>
    <section className="metric-grid"><Metric icon={Apple} tone="coral" label="今日摄入" value={calories} unit="kcal" caption="饮食热量记录" /><Metric icon={Dumbbell} tone="blue" label="活动时长" value={duration} unit="分钟" caption="累计运动时间" /><Metric icon={HeartPulse} tone="yellow" label="情绪指数" value={mood} unit={mood === '--' ? '' : '/ 10'} caption="最近一次记录" /></section>
    <section className="dashboard-grid"><div className="panel trend-panel"><div className="panel-head"><div><p className="eyebrow">WEEKLY RHYTHM</p><h2>本周健康节奏</h2></div><button className="quiet-button" onClick={() => setView('sport')}>查看记录 <ChevronRight size={16} /></button></div><div className="chart"><div className="chart-axis"><span>100</span><span>50</span><span>0</span></div><div className="bars">{[42, 63, 48, 78, 56, 86, 68].map((height, index) => <div className="bar-wrap" key={index}><div className={index === 5 ? 'bar active' : 'bar'} style={{height:`${height}%`}}/><span>{['一','二','三','四','五','六','日'][index]}</span></div>)}</div></div></div>
      <div className="panel assistant-card"><div className="assistant-top"><span className="ai-badge"><Sparkles size={16} /></span><span>AI 健康助手</span></div><h2>今天想从哪里开始？</h2><p>告诉我你的状态，我会结合健康记录给你一条可执行的建议。</p><button className="dark-button" onClick={() => setView('ai')}>问问 AI <ArrowUpRight size={17} /></button></div></section>
    <section className="panel recent-panel"><div className="panel-head"><div><p className="eyebrow">RECENTLY ADDED</p><h2>最近记录</h2></div></div><RecentRows data={data} onAdd={onAdd} /></section></>;
}

function Metric({ icon: Icon, tone, label, value, unit, caption }) { return <div className="metric"><span className={`metric-icon ${tone}`}><Icon size={21}/></span><div><p>{label}</p><strong>{value}<small>{unit}</small></strong><span>{caption}</span></div></div>; }
function RecentRows({ data, onAdd }) { const rows = useMemo(() => [...data.food.map(x=>({...x,type:'food',label:x.foodName,time:x.eatTime})),...data.sport.map(x=>({...x,type:'sport',label:x.sportName,time:x.recordTime})),...data.mood.map(x=>({...x,type:'mood',label:x.moodDesc,time:x.recordTime}))].sort((a,b)=>new Date(b.time)-new Date(a.time)).slice(0,4),[data]); if (!rows.length) return <div className="empty-state"><p>还没有记录，先从一件小事开始。</p><div><button className="outlined" onClick={()=>onAdd('food')}>添加饮食</button><button className="outlined" onClick={()=>onAdd('sport')}>添加运动</button></div></div>; return <div className="recent-rows">{rows.map((item, index)=>{const Icon=item.type==='food'?Apple:item.type==='sport'?Dumbbell:HeartPulse; return <div className="recent-row" key={`${item.type}-${item.id}-${index}`}><span className={`record-icon ${item.type}`}><Icon size={17}/></span><div><strong>{item.label || '未命名记录'}</strong><p>{formatTime(item.time)}</p></div><span className="record-tag">{item.type === 'food' ? `${item.cal || 0} kcal` : item.type === 'sport' ? `${item.duration || 0} 分钟` : `${item.moodScore || '--'} / 10`}</span></div>})}</div>; }

function FoodEditModal({ item, onClose, onSaved }) { const [form, setForm] = useState({ foodName: item.foodName || '', cal: item.cal ?? '', note: item.note || '', eatTime: item.eatTime ? new Date(item.eatTime).toISOString().slice(0, 16) : now() }); const [error, setError] = useState(''); const submit = async (event) => { event.preventDefault(); setError(''); try { await api.updateFood(item.id, { ...form, cal: Number(form.cal), eatTime: `${form.eatTime}:00` }); await onSaved(); onClose(); } catch (err) { setError(err.message || '保存失败'); } }; return <Modal title="编辑饮食记录" onClose={onClose}><form className="record-form" onSubmit={submit}><label>食物名称<input required value={form.foodName} onChange={e => setForm({...form, foodName: e.target.value})} /></label><label>热量（kcal）<input required type="number" min="0" value={form.cal} onChange={e => setForm({...form, cal: e.target.value})} /></label><label>用餐时间<input required type="datetime-local" value={form.eatTime} onChange={e => setForm({...form, eatTime: e.target.value})} /></label><label>备注<input value={form.note} onChange={e => setForm({...form, note: e.target.value})} /></label>{error && <p className="notice">{error}</p>}<button className="primary wide">保存修改 <ArrowUpRight size={17} /></button></form></Modal>; }

function WorkoutScreenshotModal({ onClose, onSaved }) {
  const [file,setFile]=useState(null),[preview,setPreview]=useState(''),[analyzing,setAnalyzing]=useState(false),[error,setError]=useState(''),[result,setResult]=useState(null),[form,setForm]=useState(null);
  const selectFile=(event)=>{const next=event.target.files?.[0];if(!next)return;if(next.size>8*1024*1024){setError('图片不能超过 8MB');return;}setFile(next);setPreview(URL.createObjectURL(next));setResult(null);setForm(null);setError('');};
  const analyze=async()=>{if(!file)return;setAnalyzing(true);setError('');try{const response=await api.identifyWorkout(file);const parsed=JSON.parse(response.reply.replace(/```json|```/g,'').trim());const recordTime=parsed.recordTime&&/^\d{4}-\d{2}-\d{2}T/.test(parsed.recordTime)?parsed.recordTime.slice(0,16):now();setResult({ ...parsed, disclaimer:response.disclaimer });setForm({sportName:parsed.sportName||'运动训练',duration:parsed.duration??'',calorie:parsed.calorie??'',recordTime,remark:`手环截图导入${parsed.summary?`：${parsed.summary}`:''}`});}catch(err){setError(err.message||'识别失败，请确认截图清晰且包含运动数据');}finally{setAnalyzing(false);}};
  const save=async(event)=>{event.preventDefault();setError('');try{await api.addSport({...form,duration:Number(form.duration),calorie:Number(form.calorie),recordTime:`${form.recordTime}:00`});await onSaved();onClose();}catch(err){setError(err.message||'导入失败');}};
  return <Modal title="导入手环运动截图" onClose={onClose}><div className="workout-import"><label className="image-upload">选择手环或运动 App 截图<input type="file" accept="image/*" onChange={selectFile}/><small>仅上传截图用于本次识别，图片不能超过 8MB。</small></label>{preview&&<img className="workout-preview" src={preview} alt="运动截图预览"/>}{file&&!result&&<button className="primary wide" onClick={analyze} disabled={analyzing}>{analyzing?'正在识别运动数据...':'分析截图并生成建议'}</button>}{result&&<><section className="workout-result"><p className="eyebrow">ANALYSIS RESULT</p><strong>{result.summary||'已识别截图中的运动数据'}</strong><p>{result.suggestion||'建议根据自身状态安排适量运动，并注意补水与休息。'}</p><small>{result.disclaimer}</small></section><form className="record-form" onSubmit={save}><label>运动类型<input required value={form.sportName} onChange={e=>setForm({...form,sportName:e.target.value})}/></label><div className="form-split"><label>时长（分钟）<input required min="1" type="number" value={form.duration} onChange={e=>setForm({...form,duration:e.target.value})}/></label><label>消耗热量（kcal）<input required min="0" type="number" value={form.calorie} onChange={e=>setForm({...form,calorie:e.target.value})}/></label></div><label>运动时间<input required type="datetime-local" value={form.recordTime} onChange={e=>setForm({...form,recordTime:e.target.value})}/></label><label>备注<input value={form.remark} onChange={e=>setForm({...form,remark:e.target.value})}/></label><button className="primary wide">确认并导入运动记录 <ArrowUpRight size={17}/></button></form></>}{error&&<p className="notice">{error}</p>}</div></Modal>;
}

function Records({ type, records, onAdd, onEdit, onDelete, onImportWorkout }) { const config={food:{title:'饮食记录',desc:'了解每一餐的能量与选择。',icon:Apple},sport:{title:'运动记录',desc:'每一次活动，都在为身体积蓄能量。',icon:Dumbbell},mood:{title:'情绪记录',desc:'看见情绪，才能更好地照顾自己。',icon:HeartPulse}}[type]; const Icon=config.icon; return <><div className="page-title"><div><p className="eyebrow">MY RECORDS</p><h1>{config.title}</h1><p>{config.desc}</p></div><div className="record-page-actions">{type==='sport'&&<button className="outlined" onClick={onImportWorkout}>导入手环截图</button>}<button className="primary" onClick={()=>onAdd(type)}><Plus size={17}/>添加记录</button></div></div><section className="panel record-list"><div className="panel-head"><div><h2>全部记录</h2><p>{records.length} 条数据</p></div></div>{records.length ? <div className="full-rows">{records.map(item=><div className="full-row" key={item.id}><span className={`record-icon ${type}`}><Icon size={19}/></span><div className="record-main"><strong>{type==='food'?item.foodName:type==='sport'?item.sportName:item.moodDesc}</strong><p>{formatTime(type==='food'?item.eatTime:item.recordTime)} {item.note||item.remark ? `· ${item.note||item.remark}`:''}</p></div><div className="record-detail">{type==='food'?`${item.cal||0} kcal`:type==='sport'?`${item.duration||0} 分钟 · ${item.calorie||0} kcal`:`${item.moodScore||'--'} / 10`}</div>{type==='food' && <div className="row-actions"><button className="quiet-button" onClick={()=>onEdit(item)}>编辑</button><button className="quiet-button danger" onClick={()=>onDelete(item)}>删除</button></div>}</div>)}</div>:<div className="empty-state"><p>还没有{config.title}。</p><button className="outlined" onClick={()=>onAdd(type)}>添加第一条记录</button></div>}</section></>; }

function AiAssistant() { const [messages, setMessages]=useState([{role:'ai',text:'你好，我是你的健康助手。你可以问我今天的饮食、运动节奏，或告诉我现在的感受。'}]); const [value,setValue]=useState(''); const [sending,setSending]=useState(false); const [workflow,setWorkflow]=useState([]); const [report,setReport]=useState(null); const [reportError,setReportError]=useState(''); const send=async()=>{if(!value.trim()||sending)return;const question=value.trim();setMessages(current=>[...current,{role:'user',text:question}]);setValue('');setSending(true);setWorkflow(["正在判断问题需要哪些健康数据"]);try{const result=await api.aiChat(question);setWorkflow(result.workflow||[]);setMessages(current=>[...current,{role:'ai',text:result.reply,disclaimer:result.disclaimer,workflow:result.workflow}]);}catch(error){setMessages(current=>[...current,{role:'ai',text:`暂时无法获取 AI 回复：${error.message}`}]);}finally{setSending(false);}}; const generateReport=async()=>{if(sending)return;setSending(true);setReportError('');try{setReport(await api.dailyReport());}catch(error){setReportError(error.message);}finally{setSending(false);}};return <><div className="page-title"><div><p className="eyebrow">PERSONAL HEALTH AI</p><h1>AI 健康助手</h1><p>依据你的记录，获得更贴近生活的健康建议。</p></div></div><section className="report-card"><div><span className="report-icon"><FileText size={20}/></span><p className="eyebrow">TODAY'S REPORT</p><h2>生成今日健康报告</h2><p>汇总今天的饮食、运动与情绪，得到一条清晰的明日行动建议。</p></div><button className="primary" onClick={generateReport} disabled={sending}><Sparkles size={17}/>{sending?'生成中...':'一键生成'}</button></section>{reportError&&<div className="load-warning">生成报告失败：{reportError}</div>}{report&&<section className="panel generated-report"><div className="panel-head"><div><p className="eyebrow">GENERATED TODAY</p><h2>我的今日健康报告</h2></div><span className="report-model">{report.model}</span></div><div className="report-content">{report.reply}</div><small className="disclaimer">{report.disclaimer}</small></section>}<section className="ai-layout"><div className="ai-hero"><div className="ai-orb"><Bot size={40}/></div><p className="eyebrow light">YOUR PERSONAL GUIDE</p><h2>从记录出发，<br/>给出更懂你的建议。</h2><span>Agent · 按需读取个人健康数据</span></div><div className="panel chat"><div className="chat-head"><div className="ai-badge"><Sparkles size={16}/></div><div><strong>健康助手</strong><p><i/>在线</p></div></div><div className="messages">{messages.map((message,index)=><div className={`message ${message.role}`} key={index}>{message.text}{message.workflow?.length>0&&<div className="workflow-list">{message.workflow.map((step,i)=><span key={i}><i/>{step}</span>)}</div>}{message.disclaimer&&<small className="disclaimer">{message.disclaimer}</small>}</div>)}{sending&&<div className="message ai"><span className="workflow-live">{workflow.map((step,i)=><span key={i}><i/>{step}</span>)}<span><i className="pulse"/>智能体正在分析并调用工具...</span></span></div>}</div><div className="chat-input"><input disabled={sending} value={value} onChange={e=>setValue(e.target.value)} onKeyDown={e=>e.key==='Enter'&&send()} placeholder="输入你想问的问题..."/><button disabled={sending} onClick={send} title="发送"><Send size={18}/></button></div></div></section></>; }

export default function App() { const [authed,setAuthed]=useState(Boolean(localStorage.getItem('health_token')));const [view,setView]=useState('dashboard');const [modal,setModal]=useState(null);const [foodEdit,setFoodEdit]=useState(null);const [workoutImport,setWorkoutImport]=useState(false);const [data,setData]=useState({food:[],sport:[],mood:[]});const [loading,setLoading]=useState(false);const [loadError,setLoadError]=useState(''); const userId=useMemo(()=>{try { const token=localStorage.getItem('health_token'); return token ? JSON.parse(atob(token.split('.')[1].replace(/-/g,'+').replace(/_/g,'/'))).sub : null; } catch { return null; }},[authed]); const load=async()=>{if(!authed||!userId)return;setLoading(true);try{const [food,sport,mood]=await Promise.all([api.foodList(),api.sportList(userId),api.moodList(userId)]);setData({food:Array.isArray(food)?food:[],sport:Array.isArray(sport)?sport:[],mood:Array.isArray(mood)?mood:[]});setLoadError('');}catch(e){setLoadError(e.message);}finally{setLoading(false);}};useEffect(()=>{load();},[authed,userId]);const deleteFood=async(item)=>{if(!window.confirm(`确定删除“${item.foodName}”吗？`))return;try{await api.deleteFood(item.id);await load();}catch(e){setLoadError(e.message);}};if(!authed)return <Auth onSuccess={()=>setAuthed(true)}/>;const content=view==='dashboard'?<Dashboard data={data} onAdd={setModal} setView={setView}/>:view==='ai'?<AiAssistant data={data}/>:view==='coach'?<ExerciseCoach/>:<Records type={view} records={data[view]} onAdd={setModal} onEdit={setFoodEdit} onDelete={deleteFood} onImportWorkout={()=>setWorkoutImport(true)}/>;return <div className="app-shell"><aside><div className="side-brand"><span className="brand-mark"><Activity size={20}/></span><span>健康伴侣</span></div><nav>{navItems.map(([key,label,Icon])=><button key={key} className={view===key?'nav-item active':'nav-item'} onClick={()=>setView(key)}><Icon size={19}/>{label}</button>)}</nav><div className="side-bottom"><button className="profile"><span><CircleUserRound size={20}/></span><div><strong>{localStorage.getItem('health_user')||'健康用户'}</strong><small>我的账号</small></div></button><button className="nav-item logout" onClick={()=>{localStorage.removeItem('health_token');setAuthed(false)}}><LogOut size={19}/>退出登录</button></div></aside><main className="main-content">{loadError&&<div className="load-warning">数据加载失败：{loadError}。请确认后端已启动。</div>}{loading?<div className="loading">正在同步健康记录...</div>:content}</main>{modal&&<RecordForm type={modal} onClose={()=>setModal(null)} onSaved={load}/>} {foodEdit&&<FoodEditModal item={foodEdit} onClose={()=>setFoodEdit(null)} onSaved={load}/>} {workoutImport&&<WorkoutScreenshotModal onClose={()=>setWorkoutImport(false)} onSaved={load}/>}</div>; }

