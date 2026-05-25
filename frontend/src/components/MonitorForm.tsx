type MonitorFormProps = {
    name: string
    url: string
    onNameChange: (name: string) => void
    onUrlChange: (url: string) => void
    onSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void
}

function MonitorForm({
    name,
    url,
    onNameChange,
    onUrlChange,
    onSubmit,
}: MonitorFormProps) {
    return (
        <form className="monitor-form" onSubmit={onSubmit}>
            <input
                aria-label="Monitor Name"
                required type="text"
                placeholder="Monitor Name"
                value={name}
                onChange={(e) => onNameChange(e.target.value)}
            />
            <input
                aria-label="Monitor URL"
                required type="url"
                placeholder="Monitor URL"
                value={url}
                onChange={(e) => onUrlChange(e.target.value)}
            />
            <button type="submit" disabled={!name || !url}>
                Add Monitor
            </button>
        </form>
    )
}

export default MonitorForm