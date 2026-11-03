import { useEffect, useState } from 'react';
import { CircleUserRound, Camera } from 'lucide-react';
import { api } from './api';

export default function UserProfile({ onSaved }) {
  const [form, setForm] = useState({ name: '', avatar: '', gender: 'unspecified', age: '', height: '', weight: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [ready, setReady] = useState(false);
  const load = async () => {
    setLoading(true); setError('');
    try {
      const profile = await api.profile();
      setForm({ name: profile.name || localStorage.getItem('health_user') || '', avatar: profile.avatar || '', gender: profile.gender || 'unspecified', age: profile.age ?? '', height: profile.height ?? '', weight: profile.weight ?? '' });
      setReady(true);
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  };
  useEffect(() => { load(); }, []);
  const change = (key, value) => { setForm(current => ({ ...current, [key]: value })); setSuccess(''); };
  const pickAvatar = async (event) => {
    const file = event.target.files?.[0]; event.target.value = '';
    if (!file) return;
    setError(''); setSuccess('');
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type) || file.size > 5 * 1024 * 1024) { setError('请选择不超过5MB的 JPG、PNG 或 WebP 图片'); return; }
    setProcessing(true);
    let url;
    try {
      url = URL.createObjectURL(file);
      const image = new Image();
      await new Promise((resolve, reject) => { image.onload = resolve; image.onerror = reject; image.src = url; });
      const canvas = document.createElement('canvas'); canvas.width = 256; canvas.height = 256;
      const context = canvas.getContext('2d');
      const size = Math.min(image.width, image.height);
      context.fillStyle = '#ffffff'; context.fillRect(0, 0, 256, 256);
      context.drawImage(image, (image.width - size) / 2, (image.height - size) / 2, size, size, 0, 0, 256, 256);
      change('avatar', canvas.toDataURL('image/jpeg', 0.85));
    } catch { setError('图片读取失败，请更换图片'); }
    finally { if (url) URL.revokeObjectURL(url); setProcessing(false); }
  };
  const submit = async (event) => {
    event.preventDefault(); setSaving(true); setError(''); setSuccess('');
    try {
      const payload = { ...form, ...Object.fromEntries(['age', 'height', 'weight'].map(key => [key, form[key] === '' ? null : Number(form[key])])) };
      const profile = await api.saveProfile(payload);
      onSaved(profile); setSuccess('个人资料已保存');
    } catch (e) { setError(e.message); }
    finally { setSaving(false); }
  };
  const bmi = Number(form.height) > 0 && Number(form.weight) > 0 ? Number(form.weight) / (Number(form.height) / 100) ** 2 : null;
  const fat = bmi && Number(form.age) >= 18 && Number(form.age) <= 120 && ['male', 'female'].includes(form.gender) ? 1.2 * bmi + 0.23 * Number(form.age) - (form.gender === 'male' ? 10.8 : 0) - 5.4 : null;
  return <>
    <div className="page-title"><div><p className="eyebrow">MY PROFILE · 个人资料</p><h1>认识自己，更好地照顾自己。</h1><p>管理你的头像与身体信息，修改后记得保存。</p></div></div>
    {loading ? <div className="loading">正在读取个人资料...</div> : <div className="profile-layout">
      <form className="panel profile-form record-form" onSubmit={submit}>
        <h2>基本信息</h2>
        <div className="avatar-editor"><div className="profile-avatar">{form.avatar ? <img src={form.avatar} alt="个人头像预览"/> : <CircleUserRound size={54}/>}</div><div><label className="outlined avatar-upload"><Camera size={16}/> {processing ? '正在处理...' : '更换头像'}<input type="file" accept="image/jpeg,image/png,image/webp" onChange={pickAvatar} disabled={saving || processing} /></label><p>JPG / PNG / WebP，最大5MB，自动居中裁剪。</p>{form.avatar && <button type="button" className="quiet-button" disabled={saving || processing} onClick={() => change('avatar', '')}>移除头像</button>}</div></div>
        <fieldset disabled={saving || processing || !ready}>
          <label>名字<input required maxLength={50} value={form.name} onChange={e => change('name', e.target.value)} autoComplete="nickname"/></label>
          <div className="form-split"><label>性别<select value={form.gender} onChange={e => change('gender', e.target.value)}><option value="unspecified">不透露</option><option value="male">男</option><option value="female">女</option></select></label><label>年龄（岁，可选）<input type="number" min="1" max="120" step="1" value={form.age} onChange={e => change('age', e.target.value)} placeholder="估算体脂率时需要"/></label></div>
          <div className="form-split"><label>身高（厘米）<input type="number" min="50" max="250" step="0.1" value={form.height} onChange={e => change('height', e.target.value)} placeholder="例如 170"/></label><label>体重（千克）<input type="number" min="10" max="350" step="0.1" value={form.weight} onChange={e => change('weight', e.target.value)} placeholder="例如 65"/></label></div>
          {error && <p className="notice" role="alert">{error}</p>}{success && <p className="notice success" role="status">{success}</p>}
          <button className="primary wide" disabled={saving || processing}>{saving ? '正在保存...' : '保存个人资料'}</button>
        </fieldset>
        {!ready && <button type="button" className="outlined" onClick={load}>重新加载</button>}
      </form>
      <section className="panel body-summary"><p className="eyebrow">BODY INSIGHTS</p><h2>身体指标预览</h2><p className="profile-hint">随填写实时更新，保存后同步到账号。</p>
        <div className="body-value"><span>BMI · 身体质量指数</span><strong>{bmi ? bmi.toFixed(1) : '—'}</strong><small>体重（kg）÷ 身高（m）²</small></div>
        <div className="body-value"><span>估算体脂率</span><strong>{fat > 0 && fat < 100 ? `${fat.toFixed(1)}%` : '—'}</strong><small>{Number(form.age) > 0 && Number(form.age) < 18 ? '未成年人不适用此成人估算公式' : '需要身高、体重、成年年龄和男/女性别'}</small></div>
        <p className="profile-hint">采用 Deurenberg 成人估算公式：1.20 × BMI + 0.23 × 年龄 − 10.8 × 性别系数 − 5.4（男为1，女为0）。性别选项仅用于该公式的统计系数。</p>
        <p className="profile-disclaimer">仅供一般健康参考，不是实测体脂率或医学诊断。孕期、运动员及肌肉量较高等人群误差可能较大；如需准确结果，请进行专业身体成分测量。</p>
      </section>
    </div>}
  </>;
}
