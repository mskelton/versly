import clsx from 'clsx'
import Link from 'next/link'
import { cloneElement } from 'react'
import { ChevronLeft, ChevronRight } from 'react-feather'
import { assertUnreachable } from '@/lib/assert'
import { getNextChapter, getPreviousChapter } from '@/lib/bookInfo'
import { PassageRef } from '@/lib/passageRef'
import {
	ChildNode,
	Node,
	Table,
	TableCell,
	TableHeading,
} from '@/lib/types/usfm'

const styles = {
	h: 'text-2xl mb-4 mt-8 font-bold',
	p: 'mb-2 indent-4',
	pm: 'mb-2 ml-4',
	q: '-indent-4',
}

/* eslint-disable sort/object-properties */
const nodeStyles = {
	iex: 'text-base italic mb-2 text-zinc-600 dark:text-zinc-400',
	d: 'italic mb-4',
	sp: 'italic mt-4',
	p: styles.p,
	nb: styles.p,
	pm: `${styles.pm} indent-2`,
	pmo: styles.pm,
	pmc: styles.pm,
	pmr: `${styles.pm} text-right`,
	pc: 'mb-2 text-center',
	pr: 'mb-2 text-right',
	cls: 'mb-2 text-right',
	pi1: `${styles.p} ml-4`,
	pi2: `${styles.p} ml-8`,
	pi3: `${styles.p} ml-12`,
	q1: `${styles.q} pl-4`,
	q2: `${styles.q} pl-8`,
	q3: `${styles.q} pl-12`,
	q4: `${styles.q} pl-16`,
	qa: 'text-center italic',
	qr: 'text-right',
	qc: 'text-center',
	qm1: `${styles.q} pl-8`,
	qm2: `${styles.q} pl-12`,
	li1: 'ml-4',
	li2: 'ml-8',
	li3: 'ml-12',
	li4: 'ml-16',
	lim: 'ml-4',
	m: 'mb-2',
	mi: 'mb-2 ml-4',
} satisfies Partial<Record<Node[0], string>>
/* eslint-enable sort/object-properties */

type ReaderProps = {
	nodes: Node[]
	passageRef: PassageRef
	showNav?: boolean
}

export async function Reader({
	nodes,
	passageRef,
	showNav = false,
}: ReaderProps) {
	const previousHref = getPreviousChapter(passageRef)
	const nextHref = getNextChapter(passageRef)

	return (
		<>
			{showNav ? (
				<ReaderNavLink
					href={previousHref}
					icon={<ChevronLeft />}
					label="Previous chapte"
					side="left"
				/>
			) : null}

			<div className="text-gray-900 dark:text-gray-200 font-sans text-lg">
				{nodes.map((node, index) => (
					<ReaderNode key={index} node={node} />
				))}
			</div>

			{showNav ? (
				<ReaderNavLink
					href={nextHref}
					icon={<ChevronRight />}
					label="Next chapter"
					side="right"
				/>
			) : null}
		</>
	)
}

function ReaderNode({ node }: { node: Node }) {
	const [type, children] = node

	switch (type) {
		case 'cl': {
			return (
				<h2 className="mb-10 first:mt-0 mt-20 font-bold flex flex-col items-center">
					<span className="block text-lg text-zinc-600 dark:text-zinc-400 mb-2">
						{children}
					</span>
					<span className="block text-7xl">{node[2]}</span>
				</h2>
			)
		}

		case 'ms':
		case 's1':
			return <h3 className={styles.h}>{children}</h3>

		case 's2':
			return <h4 className={styles.h}>{children}</h4>

		case 's3':
			return <h5 className={styles.h}>{children}</h5>

		case 'table':
			return <ReaderTable node={node} />

		case 'b':
			return <div className="h-4" />

		default:
			return (
				<p className={`${type} leading-loose ${nodeStyles[type]}`}>
					{typeof children === 'string' ? children : renderChildren(children)}
				</p>
			)
	}
}

function ReaderChildNode({ node }: { node: ChildNode }) {
	const [type, value] = node

	switch (type) {
		case 'v':
			return (
				<span className="text-gray-500 relative align-super -top-0.5 text-xs">
					{value}&nbsp;
				</span>
			)

		case 'qs':
			return <span className="italic float-right">{value}</span>

		case 'litl':
			return <span className="float-right">{value}</span>

		case 'wj':
			return <span className="text-red-600 dark:text-red-500">{value}</span>

		case 'em':
		case 'bd':
			return <span className="font-bold">{value}</span>

		case 'bk':
		case 'qt':
		case 'sig':
		case 'sls':
		case 'tl':
		case 'it':
			return <span className="italic">{value}</span>

		case 'nd':
		case 'sc':
			return <span className="[font-variant:small-caps]">{value}</span>

		case 'sup':
			return <span className="align-super text-sm">{value}</span>

		case 't':
			return <span>{value}</span>

		default:
			assertUnreachable(node)
	}
}

function ReaderTable({ node }: { node: Table }) {
	const { bodyRows, headerRows } = node[1].reduce(
		(acc, row) => {
			if (row.every((cell) => cell[0] === 'th')) {
				acc.headerRows.push(row as TableHeading[])
			} else {
				acc.bodyRows.push(row as TableCell[])
			}

			return acc
		},
		{
			bodyRows: [] as TableCell[][],
			headerRows: [] as TableHeading[][],
		},
	)

	return (
		<table>
			{headerRows ? (
				<thead>
					{headerRows.map((row, index) => (
						<tr key={index}>
							{row.map((cell, index) => (
								<th key={index}>{renderChildren(cell[1])}</th>
							))}
						</tr>
					))}
				</thead>
			) : null}

			<tbody>
				{bodyRows.map((row, index) => (
					<tr key={index}>
						{row.map((cell, index) => (
							<td key={index}>{renderChildren(cell[1])}</td>
						))}
					</tr>
				))}
			</tbody>
		</table>
	)
}

function ReaderNavLink({
	href,
	icon,
	label,
	side,
}: {
	href: string
	icon: React.ReactElement<{ className: string }>
	label: string
	side: 'left' | 'right'
}) {
	return (
		<Link
			className={clsx(
				'fixed transform top-4 bottom-4 hover:bg-gray-200 dark:hover:bg-gray-800 rounded p-2 flex items-center justify-center px-8 py-2 text-gray-500 hover:text-gray-300 transition-colors duration-300',
				side === 'left' ? 'left-4' : 'right-4',
			)}
			href={href}
		>
			<span className="sr-only">{label}</span>
			{cloneElement(icon, { className: 'size-12' })}
		</Link>
	)
}

function renderChildren(nodes: ChildNode[]) {
	return nodes.map((node, index) => <ReaderChildNode key={index} node={node} />)
}
