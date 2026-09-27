import EmptyState from '../components/EmptyState';

export default function ModulePlaceholder({ title, description }) {
  return (
    <EmptyState
      title={title}
      description={description || 'This module is planned for a later build phase and isn\u2019t wired up yet — nothing here is faked, it simply doesn\u2019t exist yet.'}
    />
  );
}
