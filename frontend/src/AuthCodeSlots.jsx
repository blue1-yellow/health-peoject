import { motion, useReducedMotion } from 'motion/react';

export default function AuthCodeSlots({ label, value, onChange, type = 'text', autoComplete, placeholder }) {
  const reduceMotion = useReducedMotion();
  const characters = Array.from(value);

  return <label className="auth-slots-field">
    {label}
    <input required type={type} value={value} onChange={onChange} autoComplete={autoComplete} placeholder={placeholder} />
    <span className="auth-slots" aria-hidden="true">
      {(characters.length ? characters.slice(-12) : Array(6).fill('')).map((character, index) =>
        <motion.span className={character ? 'auth-slot filled' : 'auth-slot'} key={`${index}-${character ? 'filled' : 'empty'}`}
          initial={reduceMotion ? false : { opacity: 0, scale: .7, y: 5 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ type: 'spring', duration: .3, bounce: .2 }}>
          {type === 'password' && character ? '•' : character}
        </motion.span>)}
    </span>
  </label>;
}
